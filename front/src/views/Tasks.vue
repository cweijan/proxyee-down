<template>
  <div class="tasks">
    <div class="tasks-entry">
      <i-button type="dashed"
        icon="plus"
        class="tasks-button"
        @click="resolveVisible=true">{{ $t("tasks.createTask") }}</i-button>
      <i-button type="dashed"
        icon="ios-pause"
        class="tasks-button"
        @click="onPauseBatch">{{ $t("tasks.pauseDownloads") }}</i-button>
      <i-button type="dashed"
        icon="ios-play"
        class="tasks-button"
        @click="onResumeBatch">{{ $t("tasks.continueDownloading") }}</i-button>
      <i-button type="dashed"
        icon="ios-trash"
        class="tasks-button"
        @click="onDeleteBatch">{{ $t("tasks.deleteTask") }}</i-button>
      <i-button type="dashed"
        icon="ios-trash"
        class="tasks-button"
        @click="doClearAll">{{ $t("tasks.clearAll") }}</i-button>
    </div>

    <Table :taskList="taskList"
      ref="taskTable"
      @on-delete="onDelete"
      @on-pause="onPause"
      @on-resume="onResume"
      @on-open="onOpen"
      @on-run="onRun" />

    <Modal v-model="deleteModal"
      :title="$t('tasks.deleteTask')">
      <Checkbox :value="delFile"
        checked="checked"></Checkbox>
      <span @click="delFile=!delFile">{{ $t('tasks.deleteTaskTip') }}</span>
      <div slot="footer">
        <Button type="primary"
          @click="doDelete(delTaskId)">{{ $t('tip.ok') }}</Button>
        <Button @click="deleteModal=false">{{ $t('tip.cancel') }}</Button>
      </div>
    </Modal>

    <Resolve v-model="resolveVisible" />
    <Create :request="createForm.request"
      :response="createForm.response"
      @close="$router.push('/');" />
  </div>
</template>

<script>
import Table from '../components/Table'
import Resolve from '../components/Task/Resolve'
import Create from '../components/Task/Create'
import { showFile, runFile } from '../common/native'

export default {
  name: 'tasks',
  components: {
    Table,
    Resolve,
    Create
  },

  mounted() {
    // Download progress per second
    this.intervalId = setInterval(() => {
      if (!this.taskList) {
        return
      }

      // Filter out the list of task IDs being downloaded
      const downloadingIds = this.taskList.filter(task => task.info.status === 1).map(task => task.id)

      this.$noSpinHttp
        .get(window.location.protocol + '//' + window.location.hostname + ':26339/tasks?status=1')
        .then(result => {
          // Get the list of task IDs that the server is downloading
          const serverDownloadingIds = result.data.map(task => task.id)

          serverDownloadingIds.forEach(serverTaskId => {
            if (downloadingIds.findIndex(localTaskId => localTaskId == serverTaskId) === -1) {
              downloadingIds.push(serverTaskId)
            }
          })

          if (downloadingIds && downloadingIds.length) {
            this.$noSpinHttp
              .get(
                window.location.protocol +
                  '//' +
                  window.location.hostname +
                  ':26339/tasks/progress?ids=' +
                  downloadingIds
              )
              .then(result => {
                // Match and update the task information being downloaded
                result.data.forEach(task => {
                  const index = this.getIndexByTaskId(task.id)
                  if (index >= 0) {
                    this.taskList[index].info = task.info
                    this.taskList[index].response = task.response
                  } else {
                    // Load newly created tasks
                    this.$noSpinHttp
                      .get(window.location.protocol + '//' + window.location.hostname + ':26339/tasks/' + task.id)
                      .then(result => this.taskList.push(result.data))
                  }
                })
              })
          }
        })
        .catch(error => {
          if (!error.response || error.response.status === 504) {
            clearInterval(this.intervalId)
          }
        })
    }, 1000)
  },

  destroyed() {
    clearInterval(this.intervalId)
  },

  data() {
    return {
      taskList: [],
      deleteModal: false,
      delTaskId: '',
      delFile: true,
      resolveVisible: false,
      createForm: {
        request: null,
        response: null
      },
      intervalId: ''
    }
  },

  watch: {
    $route() {
      this.onRouteChange(this.$route.query)
    }
  },

  methods: {
    showResolve() {
      this.resolveVisible = true
    },
    doClearAll() {
      this.delFile = false
      var ids = this.$refs.taskTable.taskList
        .filter(task => {
          return task.info.status == 4
        })
        .map(task => {
          return task.id
        })
        .join(',')
      if (ids.length > 0) this.doDelete(ids)
    },
    getAllTask() {
      this.$noSpinHttp
        .get(window.location.protocol + '//' + window.location.hostname + ':26339/tasks')
        .then(result => (this.taskList = result.data))
    },
    onRouteChange(query) {
      const flag = !!(query.request && query.response)
      this.createForm = {
        request: flag ? JSON.parse(query.request) : null,
        response: flag ? JSON.parse(query.response) : null
      }
      flag || this.getAllTask()
    },

    onPause(task) {
      this.doPause(task.id)
    },

    onResume(task) {
      this.doResume(task.id)
    },

    onDelete(task) {
      this.delTaskId = task.id
      this.delFile = true
      this.deleteModal = true
    },

    onOpen(task) {
      showFile(`${task.config.filePath}/${task.response.fileName}`)
    },
    onRun(task) {
      runFile(`${task.config.filePath}/${task.response.fileName}`)
    },
    onPauseBatch() {
      const ids = this.getCheckedIds()
      if (ids) {
        this.doPause(ids)
      }
    },

    onResumeBatch() {
      const ids = this.getCheckedIds()
      if (ids) {
        this.doResume(ids)
      }
    },

    onDeleteBatch() {
      const ids = this.getCheckedIds()
      if (ids) {
        this.delTaskId = this.getCheckedIds()
        this.delFile = false
        this.deleteModal = true
      }
    },

    doPause(ids) {
      this.$http.put( window.location.protocol +'//' +window.location.hostname +`:26339/tasks/${ids}/pause`)
    },

    doResume(ids) {
      this.$http.put(window.location.protocol +'//' +window.location.hostname +`:26339/tasks/${ids}/resume`).then(result => {
        const { pauseIds, resumeIds } = result.data
        const modifyTaskStatus = (behavior, status) => {
          result.data[behavior].forEach(id => {
            const index = this.getIndexByTaskId(id)
            if (index >= 0) {
              this.taskList[index].info.status = status
            }
          })
        }
        pauseIds && modifyTaskStatus('pauseIds', 2)
        resumeIds && modifyTaskStatus('resumeIds', 1)
      })
    },

    doDelete(ids) {
      this.$http
        .delete(window.location.protocol +'//' +window.location.hostname +`:26339/tasks/${ids}?delFile=${this.delFile}`)
        .then(() => {
          ids.split(',').forEach(id => {
            const index = this.getIndexByTaskId(id)
            if (index >= 0) {
              this.taskList.splice(index, 1)
            }
          })
        })
        .finally((this.deleteModal = false))
    },

    getCheckedIds() {
      return this.$refs.taskTable
        .getCheckedTasks()
        .map(task => task.id)
        .join(',')
    },

    getIndexByTaskId(taskId) {
      return this.taskList.findIndex(t => t.id === taskId)
    }
  },

  created() {
    this.onRouteChange(this.$route.query)
  }
}
</script>

<style lang="less" scoped>
.tasks-entry {
  margin-bottom: 20px;
  .tasks-button {
    margin-right: 10px;
    &:last-of-type {
      margin-right: 0;
    }
  }
}

.ivu-table {
  td {
    background-color: inherit;
  }
}
</style>

<style lang="less">
.tasks {
  .ivu-table {
    .taskList {
      // background-color: yellow;
    }
    td {
      background-color: inherit;
    }
  }
}
</style>
