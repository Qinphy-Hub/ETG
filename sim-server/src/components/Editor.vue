<script setup lang="ts">
import {onMounted, ref, onUpdated} from "vue";
import * as monaco from 'monaco-editor'

const props = defineProps({
  text: String,
  language: String,
})
let $backTo = defineEmits(['change'])
const editorContainer = ref<any>(null)

let editor: any
onMounted(() => {
  editor = monaco.editor.create(editorContainer.value, {
    value: props.text || '',
    language: props.language || 'python',
    minimap: {
      enabled: true,
    },
    colorDecorators: true,		//颜色装饰器
    readOnly: false,			//是否开启已读功能
    theme: "vs-dark",
    foldingHighlight: true, // 折叠等高线
    foldingStrategy: "indentation", // 折叠方式  auto | indentation
    showFoldingControls: "always", // 是否一直显示折叠 always | mouseover
    disableLayerHinting: true, // 等宽优化
    emptySelectionClipboard: false, // 空选择剪切板
    selectionClipboard: false, // 选择剪切板
    automaticLayout: true, // 自动布局
    codeLens: false, // 代码镜头
    scrollBeyondLastLine: false, // 滚动完最后一行后再滚动一屏幕
    accessibilitySupport: "off", // 辅助功能支持  "auto" | "off" | "on"
    lineNumbers: "on", // 行号 取值： "on" | "off" | "relative" | "interval" | function
    lineNumbersMinChars: 5, // 行号最小字符   number
    fontSize: 20
  })

  editor.onDidChangeModelContent(() => {
    $backTo('change', editor.getValue())
  })
})

onUpdated(() => {
  editor.getModel().setValue(props.text)
})

</script>

<template>
<div ref="editorContainer" style="width: 100%; height: 100%; text-align: left"></div>
</template>

<style scoped>

</style>
