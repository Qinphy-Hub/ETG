<template>
  <div>
    <div ref="indexChart" style="width: 450px; height: 500px"></div>
  </div>
</template>
<script setup lang="ts">
import { onMounted, ref, watch, computed } from "vue";
import * as echarts from "echarts";
const props = defineProps({
  rawData: {},
});
const xdata = ref([]);
const ydata = ref([]);
const initSetting = () => {
  const useData = JSON.parse(JSON.stringify(props.rawData));
  xdata.value = useData.xAxis.data
  ydata.value = useData.series[0].data
  console.log(xdata.value)
  console.log(ydata.value)
};
initSetting();
const indexChart = ref();
const initWaterChart = async () => {
  const e = indexChart.value;
  if (!e) return;
  const myECharts = echarts.init(e);
  const option = {
    tooltip: {
      trigger: "axis",
    },
    xAxis: {
      data: xdata.value,
    },
    yAxis: {},
    dataZoom: [
      {},
      {
        type: "inside",
      },
    ],
    toolbox: {
      left: "left",
      feature: {
        dataZoom: {
          yAxisIndex: "none",
        },
        restore: {},
        saveAsImage: {},
      },
    },
    series: [
      {
        name: "Power",
        type: "line",
        data: ydata.value,
      },
    ],
  };
  myECharts.setOption(option);
};
onMounted(() => {
  initWaterChart();
});
</script>
<style scoped></style>