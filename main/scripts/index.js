#!/usr/bin/env node

const exec = require("./process")

const { resolve } = require('path');

exec(`java -jar proxyee-down-main.jar`, { cwd: resolve(__dirname, "..", "target") }).on("data", data => {
    console.log(data)
}).on("error",data=>{
    console.log(data)
})