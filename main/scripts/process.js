const { spawn } = require('child_process')
const { EventEmitter } = require('events')

module.exports = (command, options) => {

    const emitter = new EventEmitter()
    const args = command.split(/[ ,]+/)
    const baseCommand = args.shift()
    const child = spawn(baseCommand, args, options)

    child.stderr.on('data', (buffer) => {
        emitter.emit("error", buffer.toString("utf8"))
    })

    child.stdout.on('data', (buffer) => {
        emitter.emit('data', buffer.toString("utf8"))
    })
    return emitter;

}