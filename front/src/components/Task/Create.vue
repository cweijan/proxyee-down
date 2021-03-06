<template>
  <Modal :title="selectOldTask?'刷新任务':$t('tasks.createTask')"
         :value="visible"
         @input="closeModal"
         @on-visible-change="init"
         :closable="false"
         :mask-closable="false">
    <Form v-if="visible"
          ref="form"
          :model="form"
          :rules="rules"
          :label-width="70">
      <FormItem v-if="sameTasks.length>0"
                prop="taskId"
                :label="$t('tasks.sameTaskList')">
        <Select v-model="form.taskId"
                clearable
                @on-change="sameTaskChange"
                :placeholder="$t('tasks.sameTaskPlaceholder')">
          <Option v-for="task in sameTasks"
                  :key="task.id"
                  :label="task.config.filePath+getFileSeparator()+task.response.fileName"
                  :value="task.id">
          </Option>
        </Select>
      </FormItem>
      <template v-if="!selectOldTask">
        <FormItem :label="$t('tasks.fileName')">
          <Input :disabled="disabledForm" v-model="temp.urlName" :autofocus='true'/>
        </FormItem>
        <FormItem :label="$t('tasks.extName')">
           <Row>
             <Col span="19">
              <Input :disabled="disabledForm" v-model="temp.extName"/>
            </Col>
             <Col span="5">
            <Select :disabled="disabledForm" v-model="temp.extName" >
                <Option :value="defaultExt">{{defaultExt}}</Option>
                <Option :value="'mp4'">mp4</Option>
              </Select>
            </Col>
           </Row>
        </FormItem>
        <FormItem :label="$t('tasks.fileSize')">
          {{ form.response.totalSize ? $numeral(form.response.totalSize).format('0.00b') : $t('tasks.unknowLeft') }}
        </FormItem>
        <FormItem :label="$t('tasks.connections')"
                  prop="config.connections">
          <Slider v-if="response.supportRange"
                  v-model="form.config.connections"
                  :disabled="disabledForm"
                  :min="2"
                  :max="256"
                  :step="2"
                  show-input/>
          <Slider v-else
                  disabled
                  v-model="form.config.connections"
                  :min="1"
                  :max="1"
                  show-input/>
        </FormItem>
        <FormItem :label="$t('tasks.filePath')"
                  prop="config.filePath">
          <FileChoose :disabled="disabledForm"
                      v-model="form.config.filePath"/>
        </FormItem>
      </template>
    </Form>
    <div slot="footer">
      <Button type="primary"
              @click="onSubmit">{{ $t('tip.ok') }}
      </Button>
      <Button @click="closeModal">{{ $t('tip.cancel') }}</Button>
    </div>
    <Spin size="large"
          fix
          v-if="load"/>
  </Modal>
</template>

<script>
import FileChoose from '../FileChoose'

export default {
  props: {
    request: {
      type: Object
    },
    response: {
      type: Object
    }
  },
  data() {
    return {
      load: false,
      selectOldTask: false,
      disabledForm: false,
      defaultExt:null,
      temp: {
        urlName: null,
        extName: null,
      },
      form: {
        taskId: undefined,
        request: this.request,
        response: this.response,
        config: {}
      },
      rules: {
        taskId: [{required: true, message: this.$t('tip.notNull')}],
        'response.fileName': [{required: true, message: this.$t('tip.notNull')}],
        'config.filePath': [{required: true, message: this.$t('tip.notNull')}]
      },
      sameTasks: []
    }
  },
  watch: {
    request() {
      this.form.request = this.request
      this.form.response = this.response
      this.setDefaultConfig()
    }
  },
  computed: {
    visible() {
      if (this.request && this.response) {
        return true
      } else {
        return false
      }
    }
  },
  components: {
    FileChoose
  },
  methods: {
    initUrlName() {
      if (!this.form.response) return
      const fileName = this.form.response.fileName
      if (!fileName) return
      const index = fileName.lastIndexOf('.')
      if (index !== -1) {
        this.temp.urlName = fileName.substr(0, index)
        this.temp.extName = fileName.substr(index + 1)
        this.defaultExt = fileName.substr(index + 1)
      } else {
        this.temp.urlName = fileName
        this.temp.extName = null
        this.defaultExt = null
      }
    },
    closeModal() {
      this.$emit('close')
    },
    onSubmit() {
      this.$refs['form'].validate(valid => {
        if (valid) {
          this.load = true
          if (this.form.taskId) {
            //refresh download request
            this.$http
                .put(
                    window.location.protocol + '//' + window.location.hostname + ':26339/tasks/' + this.form.taskId,
                    this.form.request
                )
                .then(() => {
                  this.$router.push('/')
                })
                .finally(() => {
                  this.load = false
                })
          } else {
            this.form.response.fileName = this.temp.urlName + (this.temp.extName ? '.' + this.temp.extName : '')
            //create download task
            this.$http
                .post(window.location.protocol + '//' + window.location.hostname + ':26339/tasks', this.form)
                .then(() => {
                  this.$router.push('/')
                })
                .finally(() => {
                  this.load = false
                })
          }
        }
      })
    },
    async init(visible) {
      this.initUrlName()
      //reset params
      this.sameTasks = []
      this.form.taskId = undefined
      this.disabledForm = false
      if (visible) {
        //check same task
        const {data: downTasks} = await this.$http.get(
            window.location.protocol + '//' + window.location.hostname + ':26339/tasks?status=1,2,3'
        )
        this.sameTasks = downTasks
            ? downTasks.filter(task => task.response.supportRange && task.response.totalSize === this.response.totalSize)
            : []
        if (this.sameTasks.length > 0) {
          // this.selectOldTask = true
          this.$Modal.confirm({
            title: this.$t('tip.tip'),
            content: this.$t('tasks.checkSameTask'),
            okText: this.$t('tip.ok'),
            cancelText: this.$t('tip.cancel'),
            onOk:()=> {
              this.selectOldTask = true
              if(this.sameTasks.length==1){
                this.form.taskId=this.sameTasks[0].id
                this.onSubmit()
              }
            },
            onCancel:() =>{
              this.sameTasks = []
            }
          })
        }
      }
    },
    getFileSeparator() {
      if (window.navigator.platform.indexOf('Win') != -1) {
        return '\\'
      } else {
        return '/'
      }
    },
    sameTaskChange(taskId) {
      const oldTask = this.sameTasks.find(task => task.id == taskId)
      if (oldTask) {
        this.form.config = {...oldTask.config}
        this.selectOldTask = false
        this.disabledForm = true
      } else {
        this.selectOldTask = true
      }
    },
    setDefaultConfig() {
      this.$noSpinHttp
          .get(window.location.protocol + '//' + window.location.hostname + ':26339/config')
          .then(result => {
            const serverConfig = result.data
            this.form.config = {
              filePath: serverConfig.filePath,
              connections: serverConfig.connections,
              timeout: serverConfig.timeout,
              retryCount: serverConfig.retryCount,
              autoRename: serverConfig.autoRename,
              speedLimit: serverConfig.speedLimit
            }
          })
    }
  },
  created() {
    this.setDefaultConfig()
    this.init(this.visible)
  }
}
</script>