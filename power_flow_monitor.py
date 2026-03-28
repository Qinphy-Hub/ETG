#!/usr/bin/env python3
# -*- coding: utf-8 -*-

import json
import time
import os
import math
from watchdog.observers import Observer
from watchdog.events import FileSystemEventHandler
from datetime import datetime
from typing import Dict, Any, List, Tuple, Optional

# 导入pandapower
import pandapower as pp
from pandapower import create_empty_network
from pandapower.run import runpp,runopp

# ==================== 配置参数 ====================
DEVICE_FILE = "D:/Simulation/user_6/workspace_46/data/devices.json"
LINES_FILE = "D:/Simulation/user_6/workspace_46/data/lines.json"

# ==================== 元件类型定义 ====================
PANDAPOWER_COMPONENTS = {
    'bus': {
        'required': [],
        'optional': ['vn_kv','is_service', 'min_vm_pu', 'max_vm_pu', 'name', 'geodata'],
        'numeric_fields': ['vn_kv', 'min_vm_pu', 'max_vm_pu']
    },
    'load': {
        'required': ['p_mw'],
        'optional': ['q_mvar', 'const_z_percent', 'const_i_percent', 'scaling',
                    'type', 'is_service', 'max_p_mw', 'min_p_mw', 'max_q_mvar',
                    'min_q_mvar', 'controllable'],
        'numeric_fields': ['p_mw', 'q_mvar', 'const_z_percent', 'const_i_percent',
                          'scaling', 'max_p_mw', 'min_p_mw', 'max_q_mvar', 'min_q_mvar']
    },
    'transformer': {
        'required': ['hv_bus', 'lv_bus', 'sn_mva', 'vn_hv_kv', 'vn_lv_kv',
                    'vk_percent', 'vkr_percent', 'pfe_kw', 'i0_percent'],
        'optional': ['shift_degree', 'tap_side', 'tap_neutral', 'tap_min',
                    'tap_max', 'tap_step_percent', 'tap_step_degree', 'tap_changer_type',
                    'oltc', 'is_service', 'power_station_unit', 'leakage_resistance_ratio_hv',
                    'leakage_reactance_ratio_hv', 'max_loading_percent', 'id'],
        'numeric_fields': ['sn_mva', 'vn_hv_kv', 'vn_lv_kv', 'vk_percent', 'vkr_percent',
                          'pfe_kw', 'i0_percent', 'shift_degree', 'tap_neutral', 'tap_min',
                          'tap_max', 'tap_step_percent', 'tap_step_degree',
                          'leakage_resistance_ratio_hv', 'leakage_reactance_ratio_hv',
                          'max_loading_percent']
    },
    'transformer3': {
        'required': ['hv_bus', 'lv_bus'],
        'optional': ['sn_hv_mva', 'sn_mv_mva', 'sn_lv_mva', 'vn_hv_kv', 'vn_mv_kv', 'vn_lv_kv',
                    'vk_hv_percent', 'vk_mv_percent', 'vk_lv_percent',
                    'vkr_hv_percent', 'vkr_mv_percent', 'vkr_lv_percent',
                    'pfe_kw', 'i0_percent', 'shift_mv_degree', 'shift_lv_degree',
                    'tap_side', 'tap_neutral', 'tap_min', 'tap_max', 'tap_step_percent',
                    'tap_changer_type', 'oltc', 'is_service', 'id'],
        'numeric_fields': ['sn_hv_mva', 'sn_mv_mva', 'sn_lv_mva', 'vn_hv_kv', 'vn_mv_kv', 'vn_lv_kv',
                          'vk_hv_percent', 'vk_mv_percent', 'vk_lv_percent',
                          'vkr_hv_percent', 'vkr_mv_percent', 'vkr_lv_percent',
                          'pfe_kw', 'i0_percent', 'shift_mv_degree', 'shift_lv_degree',
                          'tap_neutral', 'tap_min', 'tap_max', 'tap_step_percent']
    },
    'source': {
        'required': ['p_mw'],
        'optional': ['vm_pu', 'max_p_mw', 'min_p_mw', 'max_q_mvar', 'min_q_mvar',
                    'scaling', 'is_service', 'power_station_trafo'],
        'numeric_fields': ['p_mw', 'vm_pu', 'max_p_mw', 'min_p_mw', 'max_q_mvar',
                          'min_q_mvar', 'scaling', 'power_station_trafo']
    },
    'outer': {
        'required': [],
        'optional': ['vm_pu', 'va_degree', 'max_p_mw', 'min_p_mw', 'max_q_mvar',
                    'min_q_mvar', 'is_service'],
        'numeric_fields': ['vm_pu', 'va_degree', 'max_p_mw', 'min_p_mw', 'max_q_mvar', 'min_q_mvar']
    },
    'solar': {
        'required': ['p_mw'],
        'optional': ['vm_pu', 'max_p_mw', 'min_p_mw', 'max_q_mvar', 'min_q_mvar',
                    'scaling', 'is_service', 'power_station_trafo'],
        'numeric_fields': ['p_mw', 'vm_pu', 'max_p_mw', 'min_p_mw', 'max_q_mvar',
                          'min_q_mvar', 'scaling', 'power_station_trafo']
    },
    'wind': {
        'required': ['p_mw'],
        'optional': ['vm_pu', 'max_p_mw', 'min_p_mw', 'max_q_mvar', 'min_q_mvar',
                    'scaling', 'is_service', 'power_station_trafo'],
        'numeric_fields': ['p_mw', 'vm_pu', 'max_p_mw', 'min_p_mw', 'max_q_mvar',
                          'min_q_mvar', 'scaling', 'power_station_trafo']
    }
}


# ==================== 文件监控处理器 ====================
class JSONFileHandler(FileSystemEventHandler):
    """同时监控设备文件和线路文件"""

    def __init__(self, device_file: str, lines_file: str):
        self.device_file = device_file
        self.lines_file = lines_file
        self.last_modified_device = 0
        self.last_modified_lines = 0
        self.last_process_time = 0

    def on_modified(self, event):
        """任一文件被修改时触发"""
        current_time = time.time()
        # 防止频繁处理
        if current_time - self.last_process_time < 2:
            return

        #检测设备文件更新
        if event.src_path == self.device_file:
            if current_time - self.last_modified_device > 1:
                self.last_modified_device = current_time
                self.last_process_time = current_time
                self.process_files()

        #检测线路文件更新
        elif event.src_path == self.lines_file:
            if current_time - self.last_modified_lines > 1:
                self.last_modified_lines = current_time
                self.last_process_time = current_time
                self.process_files()

    def process_files(self):
        try:
            device_data = read_json_file(self.device_file)
            if device_data is None:
                return
            lines_data = read_json_file(self.lines_file)
            if lines_data is None:
                return

            #converted_devices有效设备元件，invalid_devices无效设备元件
            converted_devices, invalid_devices = convert_and_check_data(device_data)
            if invalid_devices:
                print_invalid_data(invalid_devices)
            else:
                print("\n所有设备数据均有效")

            net = create_pandapower_network(converted_devices, lines_data)

            #调试用，看电网现在究竟有什么东西
            print_network_stats(net)

            #运行潮流计算
            perform_power_flow(net)

        except Exception as e:
            print(f"文件读取失败: {str(e)}")
            import traceback
            traceback.print_exc()


# ==================== 通用文件读取函数 ====================
#核心json转dic数据格式
def read_json_file(file_path: str) -> Optional[Dict[str, Any]]:
    """读取JSON文件，失败返回None"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            data = json.load(f)
            print(f"读取文件成功: {os.path.basename(file_path)} ({len(data)} 个条目)")
            return data
    except FileNotFoundError:
        print(f"文件不存在: {file_path}")
        return None
    except json.JSONDecodeError as e:
        print(f"JSON解析错误: {str(e)}")
        return None


# ==================== 设备数据检查与转换 ====================
def check_component_validity(comp_id: str, comp_data: Dict[str, Any]) -> Tuple[bool, List[str]]:
    """检查元件数据的有效性"""
    errors = []
    comp_type = comp_data.get('type')

    if not comp_type:
        errors.append("缺少type字段")
        return False, errors

    if comp_type not in PANDAPOWER_COMPONENTS:
        errors.append(f"未知的元件类型: {comp_type}")
        return False, errors

    # 检查必需参数
    required_fields = PANDAPOWER_COMPONENTS[comp_type]['required']
    properties = comp_data.get('properties')
    if properties is None:
        properties = {}  # 处理 None

    for field in required_fields:
        if field not in properties:
            errors.append(f"缺少必需参数: {field}")

    # 检查数值字段
    numeric_fields = PANDAPOWER_COMPONENTS[comp_type]['numeric_fields']
    for field in numeric_fields:
        if field in properties:
            value = properties[field]
            if isinstance(value, str):
                try:
                    float(value)
                except ValueError:
                    errors.append(f"{field} 的值 '{value}' 不是有效的数值")
            elif not isinstance(value, (int, float)):
                errors.append(f"{field} 的值 '{value}' 不是数值类型")

    # 检查点坐标
    if 'point' in comp_data:
        point = comp_data['point']
        if not isinstance(point, list) or len(point) != 2:
            errors.append("point 应为包含2个元素的列表")
        else:
            try:
                float(point[0])
                float(point[1])
            except (ValueError, TypeError):
                errors.append("point 坐标应为数值")

    return len(errors) == 0, errors


def convert_and_check_data(data: Dict[str, Any]) -> Tuple[Dict[str, Any], Dict[str, List[str]]]:
    """转换设备数据（字符串数字转数值）并检查有效性"""
    converted = {}
    invalid_components = {}

    for comp_id, comp_data in data.items():
        converted_comp = comp_data.copy()

        # 处理 properties 为 None 的情况
        if 'properties' in converted_comp and converted_comp['properties'] is None:
            converted_comp['properties'] = {}

        if 'properties' in converted_comp:
            converted_props = {}
            for key, value in converted_comp['properties'].items():
                if isinstance(value, str):
                    try:
                        if '.' in value:
                            converted_props[key] = float(value)
                        else:
                            try:
                                converted_props[key] = int(value)
                            except ValueError:
                                converted_props[key] = value
                    except ValueError:
                        converted_props[key] = value
                else:
                    converted_props[key] = value
            converted_comp['properties'] = converted_props

        is_valid, errors = check_component_validity(comp_id, converted_comp)

        if not is_valid:
            invalid_components[comp_id] = errors

        converted[comp_id] = converted_comp

    return converted, invalid_components


def print_invalid_data(invalid_components: Dict[str, List[str]]):
    """打印无效设备数据"""
    print(f"\n发现 {len(invalid_components)} 个无效元件")
    for comp_id, errors in invalid_components.items():
        print(f"\n元件ID: {comp_id}")
        print(f"  问题:")
        for error in errors:
            print(f"    • {error}")


# ==================== 线路解析函数（基于坐标匹配） ====================
def build_coordinate_map(devices: Dict[str, Any]) -> Dict[Tuple[float, float], str]:
    """
    建立从坐标(四舍五入到6位小数)到设备ID的映射。
    """
    coord_map = {}
    for dev_id, dev_data in devices.items():
        point = dev_data.get('point')
        if point and len(point) == 2:
            try:
                lat = float(point[0])
                lon = float(point[1])
                key = (round(lat, 6), round(lon, 6))
                coord_map[key] = dev_id
            except (ValueError, TypeError):
                continue
    return coord_map


def find_device_by_coordinate(coord_map: Dict[Tuple[float, float], str],
                              target_lat: float, target_lon: float,
                              tolerance: float = 1e-6) -> Optional[str]:
    """
    根据坐标查找设备ID，先精确匹配四舍五入后的坐标，再尝试计算距离。
    """
    target_key = (round(target_lat, 6), round(target_lon, 6))
    if target_key in coord_map:
        return coord_map[target_key]

    # 精确匹配失败，计算最小距离（容忍微小误差）
    best_id = None
    min_dist = float('inf')
    for (lat, lon), dev_id in coord_map.items():
        dist = math.hypot(lat - target_lat, lon - target_lon)
        if dist < tolerance:
            return dev_id
        if dist < min_dist:
            min_dist = dist
            best_id = dev_id
    if min_dist < tolerance * 10:  # 允许稍大误差
        return best_id
    return None


# ==================== Pandapower网络构建 ====================

#发电机和外部电网的成本函数，仅用于pandapower官网案例测试，可忽略
def get_cost_for_device(dev_id: str, comp_type: str, props: Dict) -> float:
    """
    根据设备ID、类型和属性返回成本系数（€/MW）。
    您可以根据需要修改此函数，实现每个设备的差异化成本，无需修改JSON。
    """
    if comp_type == 'source':
        return 10.0
    elif comp_type == 'outer':
        # 示例：所有外部电网固定成本
        return 10.0
    else:
        return 0.0

def create_pandapower_network(devices: Dict[str, Any], lines: Dict[str, Any]) -> pp.pandapowerNet:
    net = create_empty_network()

    # 构建坐标映射（用于通过坐标查找设备）
    coord_map = build_coordinate_map(devices)
    print(f"\n  构建Pandapower网络...")
    print(f"   坐标映射: {len(coord_map)} 个设备具有有效坐标")

    # 1. 创建所有母线，并建立母线坐标映射
    bus_map = {}          # 设备ID -> 母线索引
    bus_coord_map = {}    # 坐标(四舍五入) -> 母线ID
    print("\n 创建母线...")
    for dev_id, dev_data in devices.items():
        if dev_data.get('type') == 'bus':
            props = dev_data.get('properties') or {}
            point = dev_data.get('point', [0, 0])
            try:
                lat = float(point[0]) if point and len(point) > 0 else 0
                lon = float(point[1]) if point and len(point) > 1 else 0
                vn_kv = float(props.get('vn_kv', 0))
                bus_idx = pp.create_bus(net, name=dev_id, vn_kv=vn_kv, geodata=(lat, lon))
                bus_map[dev_id] = bus_idx
                key = (round(lat, 6), round(lon, 6))
                bus_coord_map[key] = dev_id
                print(f"   母线 {dev_id}: {vn_kv}kV at ({lat:.6f}, {lon:.6f})")
            except Exception as e:
                print(f"   创建母线失败 {dev_id}: {str(e)}")

    print("\n 所有设备ID列表:")
    for dev_id in devices.keys():
        typ = devices[dev_id].get('type', 'unknown')
        print(f"   {dev_id} ({typ})")

    # 设备到母线的映射（通过线路信息建立）
    device_to_bus = {}

    # 2. 创建线路，同时建立设备-母线映射
    print("\n 创建线路...")
    line_count = 0
    for line_id, line_data in lines.items():
        polyline = line_data.get('polyline')
        if not polyline or len(polyline) != 2:
            print(f"   线路 {line_id} 的 polyline 格式不正确，跳过")
            continue

        try:
            # 获取两个端点坐标
            coord1, coord2 = polyline[0], polyline[1]
            lat1, lon1 = float(coord1[0]), float(coord1[1])
            lat2, lon2 = float(coord2[0]), float(coord2[1])

            # 通过坐标查找对应的设备
            dev1 = find_device_by_coordinate(coord_map, lat1, lon1, tolerance=1e-6)
            dev2 = find_device_by_coordinate(coord_map, lat2, lon2, tolerance=1e-6)

            if dev1 is None:
                print(f"    线路 {line_id} 的端点1未匹配到任何设备")
                continue
            if dev2 is None:
                print(f"    线路 {line_id} 的端点2未匹配到任何设备")
                continue

            # 检查设备是否是母线
            is_bus1 = dev1 in bus_map
            is_bus2 = dev2 in bus_map

            # 建立设备-母线映射：如果一端是母线，另一端是非母线，则非母线设备连接到该母线
            if is_bus1 and not is_bus2:
                device_to_bus[dev2] = dev1
                #print(f"    建立映射: {dev2} -> 母线 {dev1}")
            elif not is_bus1 and is_bus2:
                device_to_bus[dev1] = dev2
                #print(f"    建立映射: {dev1} -> 母线 {dev2}")
            elif not is_bus1 and not is_bus2:
                print(f"    两端都不是母线，无法建立映射")
                continue

            # 确定用于创建线路的母线ID（两端都必须是母线）
            # 如果一端非母线但已有映射，则用映射的母线；如果本身就是母线，则用自身
            bus1_id = dev1 if is_bus1 else device_to_bus.get(dev1)
            bus2_id = dev2 if is_bus2 else device_to_bus.get(dev2)

            if bus1_id is None or bus2_id is None:
                print(f"    无法确定两端母线，跳过")
                continue

            #这里还没有自动导入
            # 创建线路
            length = float(line_data.get('length', 0))
            try:
                pp.create_line(net,
                               bus_map[bus1_id],
                               bus_map[bus2_id],
                               length_km=length,
                               std_type='149-AL1/24-ST1A 110.0',
                               name=line_id,
                               max_loading_percent=50)
                line_count += 1
                typ1 = devices[dev1].get('type', 'unknown')
                typ2 = devices[dev2].get('type', 'unknown')
                print(f"    线路创建成功: {typ1}(母线{bus1_id}) <-> {typ2}(母线{bus2_id}) ({length} km)")
            except Exception as e:
                print(f"    创建线路失败: {str(e)}")

        except Exception as e:
            print(f"    处理线路异常: {str(e)}")

    # 3. 创建其他元件（负荷、外部电网等）
    print("\n 创建其他元件...")
    other_counts = {'load': 0, 'ext_grid': 0, 'gen': 0, 'sgen': 0, 'trafo': 0}

    for dev_id, dev_data in devices.items():
        comp_type = dev_data.get('type')
        if comp_type == 'bus':
            continue

        props = dev_data.get('properties') or {}
        print(f"\n  处理元件 {comp_type} ({dev_id})")

        # 确定连接的母线：优先使用线路建立的映射，否则尝试通过坐标匹配
        bus_id = None
        if dev_id in device_to_bus:
            bus_id = device_to_bus[dev_id]
            print(f"    母线id:  {bus_id}")
        else:
            # 尝试通过坐标匹配最近的母线（后备方案）
            point = dev_data.get('point')
            if point and len(point) == 2:
                try:
                    lat, lon = float(point[0]), float(point[1])
                    # 使用之前定义的 find_device_by_coordinate 在 bus_coord_map 中查找
                    bus_id = find_device_by_coordinate(bus_coord_map, lat, lon, tolerance=1e-5)
                    if bus_id:
                        print(f"    通过坐标匹配到母线 {bus_id}")
                except:
                    pass

        if bus_id is None:
            print(f"    设备无法找到连接的母线！")
            continue
        bus_idx = bus_map[bus_id]

        try:
            if comp_type == 'load':
                pp.create_load(net,
                               bus_idx,
                               p_mw=float(props.get('p_mw', 0)),
                               q_mvar=float(props.get('q_mvar', 0)),
                               name=dev_id)
                other_counts['load'] += 1
                #print(f"     负荷创建成功 (连接至母线 {bus_id})")


            elif comp_type == 'source':
                # 创建发电机 (gen)，作为 PV 节点（如有 vm_pu 则使用，否则默认 1.0）
                vm_pu = float(props.get('vm_pu', 1.0))
                p_mw = float(props.get('p_mw', 0))
                gen_idx =pp.create_gen(net,
                              bus_idx,
                              p_mw=p_mw,
                              vm_pu=vm_pu,
                              name=dev_id,
                              min_p_mw=float(props.get('min_p_mw', -1e9)),
                              max_p_mw=float(props.get('max_p_mw', 1e9)),
                              min_q_mvar=float(props.get('min_q_mvar', -1e9)),
                              max_q_mvar=float(props.get('max_q_mvar', 1e9)),
                              controllable=True)
                other_counts['gen'] += 1

                # 获取成本系数并创建成本项
                #仅测试用两个发电机不同成本
                #cost_per_mw = get_cost_for_device(dev_id, comp_type, props)
                #cost_idx = pp.create_poly_cost(net, gen_idx, 'gen', cp1_eur_per_mw=cost_per_mw)
                if gen_idx==0:
                    cost_idx = pp.create_poly_cost(net, gen_idx, 'gen', cp1_eur_per_mw=15)
                if gen_idx==1:
                    cost_idx = pp.create_poly_cost(net, gen_idx, 'gen', cp1_eur_per_mw=12)



            elif comp_type == 'outer':
                # 创建外部电网 (ext_grid)，作为平衡节点
                vm_pu = float(props.get('vm_pu', 1.0))
                ext_grid_idx =pp.create_ext_grid(net,
                                   bus_idx,
                                   vm_pu=vm_pu,
                                   name=dev_id,
                                   min_p_mw=float(props.get('min_p_mw', -1e9)),
                                   max_p_mw=float(props.get('max_p_mw', 1e9)),
                                   min_q_mvar=float(props.get('min_q_mvar', -1e9)),
                                   max_q_mvar=float(props.get('max_q_mvar', 1e9)))
                other_counts['ext_grid'] += 1
                cost_per_mw = get_cost_for_device(dev_id, comp_type, props)
                cost_idx = pp.create_poly_cost(net, ext_grid_idx, 'ext_grid', cp1_eur_per_mw=cost_per_mw)


            elif comp_type in ['solar', 'wind']:
                pp.create_sgen(net,
                               bus_idx,
                               p_mw=float(props.get('p_mw', 0)),
                               q_mvar=float(props.get('q_mvar', 0)),
                               name=dev_id,
                               type=comp_type)
                other_counts['sgen'] += 1
                print(f"     {comp_type}创建成功 (连接至母线 {bus_id})")



            elif comp_type == 'transformer':
                hv_bus_id = props.get('hv_bus')
                lv_bus_id = props.get('lv_bus')
                # 确保为字符串并去除空格
                hv_bus_id = str(hv_bus_id).strip() if hv_bus_id is not None else None
                lv_bus_id = str(lv_bus_id).strip() if lv_bus_id is not None else None
                # 检查母线是否存在
                if hv_bus_id not in bus_map:
                    print(f"    错误: 变压器 {dev_id} 的高压侧母线 '{hv_bus_id}' 未找到")
                    print(f"          现有母线ID列表: {list(bus_map.keys())}")
                    continue

                if lv_bus_id not in bus_map:
                    print(f"    错误: 变压器 {dev_id} 的低压侧母线 '{lv_bus_id}' 未找到")
                    print(f"          现有母线ID列表: {list(bus_map.keys())}")
                    continue

                try:
                    # 使用 create_transformer_from_parameters 避免 std_type 参数
                    pp.create_transformer_from_parameters(
                        net,
                        hv_bus=bus_map[hv_bus_id],
                        lv_bus=bus_map[lv_bus_id],
                        sn_mva=float(props.get('sn_mva', 0)),
                        vn_hv_kv=float(props.get('vn_hv_kv', 0)),
                        vn_lv_kv=float(props.get('vn_lv_kv', 0)),
                        vk_percent=float(props.get('vk_percent', 0)),
                        vkr_percent=float(props.get('vkr_percent', 0)),
                        pfe_kw=float(props.get('pfe_kw', 0)),
                        i0_percent=float(props.get('i0_percent', 0)),
                        shift_degree=float(props.get('shift_degree', 0)),
                        name=dev_id,
                        max_loading_percent=float(props.get('max_loading_percent', 0))
                    )
                    other_counts['trafo'] += 1
                    print(f"    变压器创建成功 (高压侧母线 {hv_bus_id}, 低压侧 {lv_bus_id})")

                except Exception as e:

                    print(f"    变压器创建失败: {str(e)}")


            elif comp_type == 'transformer3':
                # 获取三侧母线ID
                hv_bus_id = props.get('hv_bus')
                mv_bus_id = props.get('mv_bus')  # 中压侧可能不存在，设为 None
                lv_bus_id = props.get('lv_bus')

                # 确保为字符串并去除空格
                hv_bus_id = str(hv_bus_id).strip() if hv_bus_id is not None else None
                mv_bus_id = str(mv_bus_id).strip() if mv_bus_id is not None else None
                lv_bus_id = str(lv_bus_id).strip() if lv_bus_id is not None else None

                # 检查高压侧和低压侧母线必须存在（中压侧可选）
                if hv_bus_id not in bus_map:
                    print(f"    错误: 三绕组变压器 {dev_id} 的高压侧母线 '{hv_bus_id}' 未找到")
                    print(f"          现有母线ID列表: {list(bus_map.keys())}")
                    continue
                if lv_bus_id not in bus_map:
                    print(f"    错误: 三绕组变压器 {dev_id} 的低压侧母线 '{lv_bus_id}' 未找到")
                    print(f"          现有母线ID列表: {list(bus_map.keys())}")
                    continue
                if mv_bus_id is not None and mv_bus_id not in bus_map:
                    print(f"    警告: 三绕组变压器 {dev_id} 的中压侧母线 '{mv_bus_id}' 未找到，将忽略")
                    mv_bus_id = None

                try:
                    pp.create_transformer3w_from_parameters(
                        net,
                        hv_bus=bus_map[hv_bus_id],
                        mv_bus=bus_map[mv_bus_id],
                        lv_bus=bus_map[lv_bus_id],
                        sn_hv_mva=float(props.get('sn_hv_mva', 0)),
                        sn_mv_mva=float(props.get('sn_mv_mva', 0)),
                        sn_lv_mva=float(props.get('sn_lv_mva', 0)),
                        vn_hv_kv=float(props.get('vn_hv_kv', 0)),
                        vn_mv_kv=float(props.get('vn_mv_kv', 0)),
                        vn_lv_kv=float(props.get('vn_lv_kv', 0)),
                        vk_hv_percent=float(props.get('vk_hv_percent', 0)),
                        vk_mv_percent=float(props.get('vk_mv_percent', 0)),
                        vk_lv_percent=float(props.get('vk_lv_percent', 0)),
                        vkr_hv_percent=float(props.get('vkr_hv_percent', 0)),
                        vkr_mv_percent=float(props.get('vkr_mv_percent', 0)),
                        vkr_lv_percent=float(props.get('vkr_lv_percent', 0)),
                        pfe_kw=float(props.get('pfe_kw', 0)),
                        i0_percent=float(props.get('i0_percent', 0)),
                        shift_mv_degree=float(props.get('shift_mv_degree', 0)),
                        shift_lv_degree=float(props.get('shift_lv_degree', 0)),
                        name=dev_id,
                        max_loading_percent=float(props.get('max_loading_percent', 0))
                    )
                    other_counts['trafo3'] += 1

                except Exception as e:
                    print(f"    三绕组变压器创建失败 {dev_id}: {str(e)}")

        except Exception as e:
            print(f"    创建失败: {str(e)}")

    print(f"\n 网络构建完成:")
    print(f"   母线: {len(bus_map)} 个")
    print(f"   线路: {line_count} 条")
    print(f"   负荷: {other_counts['load']} 个")
    print(f"   外部电网: {other_counts['ext_grid']} 个")
    print(f"   发电机: {len(net.gen)}个")
    print(f"   静态发电机: {other_counts['sgen']} 个")
    print(f"   变压器: {other_counts['trafo']} 个")

    return net


def _get_bus_by_coordinate(dev_id: str, devices: Dict, bus_coord_map: Dict, tolerance=1e-5) -> Optional[str]:
    """仅通过坐标匹配设备对应的母线（不依赖线路映射）"""
    point = devices[dev_id].get('point')
    if not point or len(point) != 2:
        return None
    try:
        lat, lon = float(point[0]), float(point[1])
        key = (round(lat, 6), round(lon, 6))
        if key in bus_coord_map:
            return bus_coord_map[key]
        # 模糊匹配
        best = None
        min_dist = tolerance
        for (blat, blon), bid in bus_coord_map.items():
            dist = math.hypot(blat - lat, blon - lon)
            if dist < min_dist:
                min_dist = dist
                best = bid
        return best
    except:
        return None


def print_network_stats(net: pp.pandapowerNet):
    """打印网络统计信息（不进行潮流计算）"""
    print("\n" + "=" * 50)
    print("网络统计信息")
    print("=" * 50)
    print(f"母线数量: {len(net.bus)}")
    print(f"线路数量: {len(net.line)}")
    print(f"变压器数量: {len(net.trafo)}")
    print(f"负荷数量: {len(net.load)}")
    print(f"发电机数量: {len(net.gen)}")
    print(f"外部电网数量: {len(net.ext_grid)}")
    print(f"静态发电机数量: {len(net.sgen)}")

    if len(net.bus) > 0:
        print("\n母线电压等级分布:")
        vn_counts = net.bus['vn_kv'].value_counts()
        for vn, cnt in vn_counts.items():
            print(f"  {vn} kV: {cnt} 个")

    print("\n 网络构建成功")


def perform_power_flow(net):
    """运行潮流计算并打印结果"""
    if len(net.bus) == 0:
        print("\n 没有母线，无法进行潮流计算")
        return
    if len(net.ext_grid) == 0:
        print("\n 没有外部电网，潮流计算可能不收敛")

    print("\n" + "=" * 60)
    print(" 运行潮流计算...")
    try:
        runopp(net, delta=1e-16)
        print(" 潮流计算成功！")
    except Exception as e:
        print(f" 潮流计算失败: {str(e)}")
        print("\n--- 调试信息 ---")
        print("发电机数据:")
        print(net.gen[['name', 'p_mw', 'vm_pu', 'min_q_mvar', 'max_q_mvar']])
        print("\n外部电网数据:")
        print(net.ext_grid[['name', 'vm_pu']])
        print("\n负荷数据:")
        print(net.load[['name', 'p_mw', 'q_mvar']])
        print("----------------")
        return

    # 打印结果
    print("\n" + "=" * 60)
    print(" 潮流计算结果")
    print("=" * 60)

    print("外部电网结果：")
    print(net.res_ext_grid)
    print("\n发电机结果：")
    print(net.res_gen)
    print(f"\n总成本：{net.res_cost} 欧元/小时")
    print("变压器负载")
    print(net.res_trafo.loading_percent)
    print("线路负载")
    print(net.res_line.loading_percent)
    print("节点电压")
    print(net.res_bus)

    print("\n" + "=" * 60)
    print(" 潮流计算完成")



# ==================== 主程序 ====================
def main():
    """主函数：启动文件监控"""
    print("=" * 60)
    print(" 电网数据监控系统 (仅构建网络，不计算潮流)")
    print("=" * 60)
    print(f"监控设备文件: {DEVICE_FILE}")
    print(f"监控线路文件: {LINES_FILE}")
    print("按 Ctrl+C 停止监控\n")

    # 检查文件是否存在
    if not os.path.exists(DEVICE_FILE):
        print(f"警告: 设备文件不存在 - {DEVICE_FILE}")
    if not os.path.exists(LINES_FILE):
        print(f"警告: 线路文件不存在 - {LINES_FILE}")

    # 创建事件处理器
    event_handler = JSONFileHandler(DEVICE_FILE, LINES_FILE)

    # 立即处理一次
    event_handler.process_files()

    # 启动监控
    observer = Observer()
    # 监控设备文件所在目录
    device_dir = os.path.dirname(DEVICE_FILE) or '.'
    observer.schedule(event_handler, path=device_dir, recursive=False)
    # 如果线路文件在不同目录，也监控
    lines_dir = os.path.dirname(LINES_FILE) or '.'
    if lines_dir != device_dir:
        observer.schedule(event_handler, path=lines_dir, recursive=False)

    observer.start()

    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        observer.stop()
        print("\n\n🛑 监控已停止")

    observer.join()


if __name__ == "__main__":
    main()