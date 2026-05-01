// SchoolDataUtil.java
package com.example.classcourse2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchoolDataUtil {

    // 城市数据
    public static List<String> getCities() {
        return Arrays.asList("北京市", "上海市", "广州市", "深圳市", "杭州市", "南京市", "武汉市", "成都市", "重庆市","佛山市");
    }

    // 区县数据（按城市）
    public static Map<String, List<String>> getDistricts() {
        Map<String, List<String>> districts = new HashMap<>();

        districts.put("北京市", Arrays.asList("东城区", "西城区", "朝阳区", "海淀区", "丰台区", "石景山区"));
        districts.put("上海市", Arrays.asList("黄浦区", "徐汇区", "长宁区", "静安区", "普陀区", "虹口区"));
        districts.put("广州市", Arrays.asList("天河区", "越秀区", "海珠区", "荔湾区", "白云区", "黄埔区","从化区","番禺区"));
        districts.put("深圳市", Arrays.asList("福田区", "罗湖区", "南山区", "盐田区", "宝安区", "龙岗区"));
        districts.put("杭州市", Arrays.asList("上城区", "下城区", "江干区", "拱墅区", "西湖区", "滨江区"));
        districts.put("南京市", Arrays.asList("玄武区", "秦淮区", "建邺区", "鼓楼区", "浦口区", "栖霞区"));
        districts.put("武汉市", Arrays.asList("江岸区", "江汉区", "硚口区", "汉阳区", "武昌区", "青山区"));
        districts.put("成都市", Arrays.asList("锦江区", "青羊区", "金牛区", "武侯区", "成华区", "龙泉驿区"));
        districts.put("重庆市", Arrays.asList("渝中区", "大渡口区", "江北区", "沙坪坝区", "九龙坡区", "南岸区"));
        districts.put("佛山市", Arrays.asList("禅城区", "南海区", "三水区", "顺德区", "高明区"));

        return districts;
    }

    // 学校数据（按区县）
    public static Map<String, List<String>> getSchools() {
        Map<String, List<String>> schools = new HashMap<>();

        // 北京市海淀区学校
        schools.put("海淀区", Arrays.asList(
                "北京大学", "清华大学", "中国人民大学", "北京航空航天大学",
                "北京理工大学", "北京师范大学", "北京邮电大学", "北京科技大学"
        ));

        // 北京市朝阳区学校
        schools.put("朝阳区", Arrays.asList(
                "北京工业大学", "北京第二外国语学院", "北京服装学院",
                "北京联合大学", "中国传媒大学", "中央美术学院"
        ));

        // 上海市徐汇区学校
        schools.put("徐汇区", Arrays.asList(
                "上海交通大学", "复旦大学", "华东理工大学",
                "上海师范大学", "上海音乐学院", "上海戏剧学院"
        ));

        // 上海市浦东新区学校
        schools.put("浦东新区", Arrays.asList(
                "上海科技大学", "上海海事大学", "上海海洋大学",
                "上海电力大学", "上海第二工业大学", "上海建桥学院"
        ));

        // 广州市天河区学校
        schools.put("天河区", Arrays.asList(
                "华南理工大学", "华南师范大学", "华南农业大学",
                "广东工业大学", "广东技术师范大学", "广州体育学院"
        ));

        // 深圳市南山区学校
        schools.put("南山区", Arrays.asList(
                "深圳大学", "南方科技大学", "哈尔滨工业大学（深圳）",
                "北京大学深圳研究生院", "清华大学深圳研究生院"
        ));

        // 佛山市三水区学校
        schools.put("三水区", Arrays.asList(
                "佛山大学", "广东财经大学三水校区", "佛山职业技术学院",
                "广东环境保护工程职业学院", "乐平中学"
        ));

        // 其他区县可以继续添加...

        return schools;
    }
}