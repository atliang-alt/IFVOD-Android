package com.cqcsy.lgsp.utils

import com.cqcsy.lgsp.R

/**
 * 表情加载类,可自己添加多种表情，分别建立不同的map存放和不同的标志符即可
 */
object EmotionUtils {

    /**
     * key-表情文字;
     * value-表情图片资源
     */
    var emotionMap: LinkedHashMap<String, Int> = LinkedHashMap()

    /**
     * key-表情文字;
     * value-vip等级图片资源
     */
    var levelMap: LinkedHashMap<String, Int> = LinkedHashMap()

    /**
     * key-vip表情标题;
     * value-对应标题下的表情map
     */
    var vipEmotionMap: LinkedHashMap<String, LinkedHashMap<String, String>> = LinkedHashMap()

    /**
     * 根据名称获取当前vip等级图标R值
     *
     * @param imgName  名称
     * @return
     */
    fun getLevelImgByName(imgName: String?): Int {
        val integer = levelMap[imgName]
        return integer ?: -1
    }

    /**
     * 根据名称获取当前表情图标R值
     *
     * @param imgName  名称
     * @return
     */
    fun getImgByName(imgName: String?): Int {
        val integer = emotionMap[imgName]
        return integer ?: -1
    }

    /**
     * 根据名称获取当前表情图标本地路径
     *
     * @param imgName  名称
     * @return
     */
    fun getVipImgByName(imgName: String?): String {
        vipEmotionMap.forEach {
            if (it.value[imgName] != null) {
                return it.value[imgName]!!
            }
        }
        return ""
    }

    init {
        levelMap["(:level_1)"] = R.mipmap.icon_vip_level_1_min
        levelMap["(:level_2)"] = R.mipmap.icon_vip_level_1_min
        levelMap["(:level_3)"] = R.mipmap.icon_vip_level_2_min
        levelMap["(:level_4)"] = R.mipmap.icon_vip_level_3_min

        emotionMap["(:1)"] = R.drawable.emoji_1
        emotionMap["(:2)"] = R.drawable.emoji_2
        emotionMap["(:3)"] = R.drawable.emoji_3
        emotionMap["(:4)"] = R.drawable.emoji_4
        emotionMap["(:5)"] = R.drawable.emoji_5
        emotionMap["(:6)"] = R.drawable.emoji_6
        emotionMap["(:7)"] = R.drawable.emoji_7
        emotionMap["(:8)"] = R.drawable.emoji_8
        emotionMap["(:9)"] = R.drawable.emoji_9
        emotionMap["(:10)"] = R.drawable.emoji_10
        emotionMap["(:11)"] = R.drawable.emoji_11
        emotionMap["(:12)"] = R.drawable.emoji_12
        emotionMap["(:13)"] = R.drawable.emoji_13
        emotionMap["(:14)"] = R.drawable.emoji_14
        emotionMap["(:15)"] = R.drawable.emoji_15
        emotionMap["(:16)"] = R.drawable.emoji_16
        emotionMap["(:17)"] = R.drawable.emoji_17
        emotionMap["(:18)"] = R.drawable.emoji_18
        emotionMap["(:19)"] = R.drawable.emoji_19
        emotionMap["(:20)"] = R.drawable.emoji_20
        emotionMap["(:21)"] = R.drawable.emoji_21
        emotionMap["(:22)"] = R.drawable.emoji_22
        emotionMap["(:23)"] = R.drawable.emoji_23
        emotionMap["(:24)"] = R.drawable.emoji_24
        emotionMap["(:25)"] = R.drawable.emoji_25
        emotionMap["(:26)"] = R.drawable.emoji_26
        emotionMap["(:27)"] = R.drawable.emoji_27
        emotionMap["(:28)"] = R.drawable.emoji_28
        emotionMap["(:29)"] = R.drawable.emoji_29
        emotionMap["(:30)"] = R.drawable.emoji_30
        emotionMap["(:31)"] = R.drawable.emoji_31
        emotionMap["(:32)"] = R.drawable.emoji_32
        emotionMap["(:33)"] = R.drawable.emoji_33
        emotionMap["(:34)"] = R.drawable.emoji_34
        emotionMap["(:35)"] = R.drawable.emoji_35
        emotionMap["(:36)"] = R.drawable.emoji_36
        emotionMap["(:37)"] = R.drawable.emoji_37
        emotionMap["(:38)"] = R.drawable.emoji_38
        emotionMap["(:39)"] = R.drawable.emoji_39
        emotionMap["(:40)"] = R.drawable.emoji_40
        emotionMap["(:182)"] = R.drawable.emoji_182
        emotionMap["(:183)"] = R.drawable.emoji_183
        emotionMap["(:184)"] = R.drawable.emoji_184
        emotionMap["(:185)"] = R.drawable.emoji_185
        emotionMap["(:186)"] = R.drawable.emoji_186
        emotionMap["(:187)"] = R.drawable.emoji_187
        emotionMap["(:188)"] = R.drawable.emoji_188
        emotionMap["(:189)"] = R.drawable.emoji_189
        emotionMap["(:190)"] = R.drawable.emoji_190
        emotionMap["(:191)"] = R.drawable.emoji_191
        emotionMap["(:192)"] = R.drawable.emoji_192
        emotionMap["(:193)"] = R.drawable.emoji_193
        emotionMap["(:194)"] = R.drawable.emoji_194
        emotionMap["(:195)"] = R.drawable.emoji_195
        emotionMap["(:196)"] = R.drawable.emoji_196
        emotionMap["(:197)"] = R.drawable.emoji_197
        emotionMap["(:198)"] = R.drawable.emoji_198
        emotionMap["(:199)"] = R.drawable.emoji_199
        emotionMap["(:200)"] = R.drawable.emoji_200
        emotionMap["(:201)"] = R.drawable.emoji_201
        emotionMap["(:202)"] = R.drawable.emoji_202
        emotionMap["(:203)"] = R.drawable.emoji_203
        emotionMap["(:204)"] = R.drawable.emoji_204
        emotionMap["(:205)"] = R.drawable.emoji_205
        emotionMap["(:206)"] = R.drawable.emoji_206
        emotionMap["(:207)"] = R.drawable.emoji_207
        emotionMap["(:208)"] = R.drawable.emoji_208
        emotionMap["(:209)"] = R.drawable.emoji_209
        emotionMap["(:210)"] = R.drawable.emoji_210
        emotionMap["(:211)"] = R.drawable.emoji_211
        emotionMap["(:212)"] = R.drawable.emoji_212
        emotionMap["(:213)"] = R.drawable.emoji_213
        emotionMap["(:214)"] = R.drawable.emoji_214
        emotionMap["(:215)"] = R.drawable.emoji_215
        emotionMap["(:216)"] = R.drawable.emoji_216
        emotionMap["(:217)"] = R.drawable.emoji_217
        emotionMap["(:218)"] = R.drawable.emoji_218
        emotionMap["(:219)"] = R.drawable.emoji_219
        emotionMap["(:220)"] = R.drawable.emoji_220
        emotionMap["(:221)"] = R.drawable.emoji_221
        emotionMap["(:222)"] = R.drawable.emoji_222
        emotionMap["(:223)"] = R.drawable.emoji_223
        emotionMap["(:224)"] = R.drawable.emoji_224

        val vipMapOne: LinkedHashMap<String, String> = LinkedHashMap()
        vipMapOne["(:20210101)"] = "file:///android_asset/duofuxiong/duofuxiong_20210101.gif"
        vipMapOne["(:20210102)"] = "file:///android_asset/duofuxiong/duofuxiong_20210102.gif"
        vipMapOne["(:20210103)"] = "file:///android_asset/duofuxiong/duofuxiong_20210103.gif"
        vipMapOne["(:20210104)"] = "file:///android_asset/duofuxiong/duofuxiong_20210104.gif"
        vipMapOne["(:20210105)"] = "file:///android_asset/duofuxiong/duofuxiong_20210105.gif"
        vipMapOne["(:20210106)"] = "file:///android_asset/duofuxiong/duofuxiong_20210106.gif"
        vipMapOne["(:20210107)"] = "file:///android_asset/duofuxiong/duofuxiong_20210107.gif"
        vipMapOne["(:20210108)"] = "file:///android_asset/duofuxiong/duofuxiong_20210108.gif"
        vipMapOne["(:20210109)"] = "file:///android_asset/duofuxiong/duofuxiong_20210109.gif"
        vipMapOne["(:20210110)"] = "file:///android_asset/duofuxiong/duofuxiong_20210110.gif"
        vipMapOne["(:20210111)"] = "file:///android_asset/duofuxiong/duofuxiong_20210111.gif"
        vipMapOne["(:20210112)"] = "file:///android_asset/duofuxiong/duofuxiong_20210112.gif"
        vipEmotionMap["多福熊"] = vipMapOne

        val vipMapTwo: LinkedHashMap<String, String> = LinkedHashMap()
        vipMapTwo["(:20210201)"] = "file:///android_asset/huhu/huhu_20210201.gif"
        vipMapTwo["(:20210202)"] = "file:///android_asset/huhu/huhu_20210202.gif"
        vipMapTwo["(:20210203)"] = "file:///android_asset/huhu/huhu_20210203.gif"
        vipMapTwo["(:20210204)"] = "file:///android_asset/huhu/huhu_20210204.gif"
        vipMapTwo["(:20210205)"] = "file:///android_asset/huhu/huhu_20210205.gif"
        vipMapTwo["(:20210206)"] = "file:///android_asset/huhu/huhu_20210206.gif"
        vipMapTwo["(:20210207)"] = "file:///android_asset/huhu/huhu_20210207.gif"
        vipMapTwo["(:20210208)"] = "file:///android_asset/huhu/huhu_20210208.gif"
        vipMapTwo["(:20210209)"] = "file:///android_asset/huhu/huhu_20210209.gif"
        vipMapTwo["(:20210210)"] = "file:///android_asset/huhu/huhu_20210210.gif"
        vipMapTwo["(:20210211)"] = "file:///android_asset/huhu/huhu_20210211.gif"
        vipMapTwo["(:20210212)"] = "file:///android_asset/huhu/huhu_20210212.gif"
        vipMapTwo["(:20210213)"] = "file:///android_asset/huhu/huhu_20210213.gif"
        vipMapTwo["(:20210214)"] = "file:///android_asset/huhu/huhu_20210214.gif"
        vipMapTwo["(:20210215)"] = "file:///android_asset/huhu/huhu_20210215.gif"
        vipMapTwo["(:20210216)"] = "file:///android_asset/huhu/huhu_20210216.gif"
        vipMapTwo["(:20210217)"] = "file:///android_asset/huhu/huhu_20210217.gif"
        vipMapTwo["(:20210218)"] = "file:///android_asset/huhu/huhu_20210218.gif"
        vipMapTwo["(:20210219)"] = "file:///android_asset/huhu/huhu_20210219.gif"
        vipMapTwo["(:20210220)"] = "file:///android_asset/huhu/huhu_20210220.gif"
        vipEmotionMap["糊糊"] = vipMapTwo

        val vipMapThree: LinkedHashMap<String, String> = LinkedHashMap()
        vipMapThree["(:20210301)"] = "file:///android_asset/jidabai/jidabai_20210301.gif"
        vipMapThree["(:20210302)"] = "file:///android_asset/jidabai/jidabai_20210302.gif"
        vipMapThree["(:20210303)"] = "file:///android_asset/jidabai/jidabai_20210303.gif"
        vipMapThree["(:20210304)"] = "file:///android_asset/jidabai/jidabai_20210304.gif"
        vipMapThree["(:20210305)"] = "file:///android_asset/jidabai/jidabai_20210305.gif"
        vipMapThree["(:20210306)"] = "file:///android_asset/jidabai/jidabai_20210306.gif"
        vipMapThree["(:20210307)"] = "file:///android_asset/jidabai/jidabai_20210307.gif"
        vipMapThree["(:20210308)"] = "file:///android_asset/jidabai/jidabai_20210308.gif"
        vipMapThree["(:20210309)"] = "file:///android_asset/jidabai/jidabai_20210309.gif"
        vipMapThree["(:20210310)"] = "file:///android_asset/jidabai/jidabai_20210310.gif"
        vipMapThree["(:20210311)"] = "file:///android_asset/jidabai/jidabai_20210311.gif"
        vipMapThree["(:20210312)"] = "file:///android_asset/jidabai/jidabai_20210312.gif"
        vipMapThree["(:20210313)"] = "file:///android_asset/jidabai/jidabai_20210313.gif"
        vipMapThree["(:20210314)"] = "file:///android_asset/jidabai/jidabai_20210314.gif"
        vipMapThree["(:20210315)"] = "file:///android_asset/jidabai/jidabai_20210315.gif"
        vipMapThree["(:20210316)"] = "file:///android_asset/jidabai/jidabai_20210316.gif"
        vipMapThree["(:20210317)"] = "file:///android_asset/jidabai/jidabai_20210317.gif"
        vipMapThree["(:20210318)"] = "file:///android_asset/jidabai/jidabai_20210318.gif"
        vipMapThree["(:20210319)"] = "file:///android_asset/jidabai/jidabai_20210319.gif"
        vipMapThree["(:20210320)"] = "file:///android_asset/jidabai/jidabai_20210320.gif"
        vipMapThree["(:20210321)"] = "file:///android_asset/jidabai/jidabai_20210321.gif"
        vipMapThree["(:20210322)"] = "file:///android_asset/jidabai/jidabai_20210322.gif"
        vipMapThree["(:20210323)"] = "file:///android_asset/jidabai/jidabai_20210323.gif"
        vipMapThree["(:20210324)"] = "file:///android_asset/jidabai/jidabai_20210324.gif"
        vipEmotionMap["鸡大白"] = vipMapThree

        val vipMapFour: LinkedHashMap<String, String> = LinkedHashMap()
        vipMapFour["(:20210401)"] = "file:///android_asset/miaohun/miaohun_20210401.gif"
        vipMapFour["(:20210402)"] = "file:///android_asset/miaohun/miaohun_20210402.gif"
        vipMapFour["(:20210403)"] = "file:///android_asset/miaohun/miaohun_20210403.gif"
        vipMapFour["(:20210404)"] = "file:///android_asset/miaohun/miaohun_20210404.gif"
        vipMapFour["(:20210405)"] = "file:///android_asset/miaohun/miaohun_20210405.gif"
        vipMapFour["(:20210406)"] = "file:///android_asset/miaohun/miaohun_20210406.gif"
        vipMapFour["(:20210407)"] = "file:///android_asset/miaohun/miaohun_20210407.gif"
        vipMapFour["(:20210408)"] = "file:///android_asset/miaohun/miaohun_20210408.gif"
        vipMapFour["(:20210409)"] = "file:///android_asset/miaohun/miaohun_20210409.gif"
        vipMapFour["(:20210410)"] = "file:///android_asset/miaohun/miaohun_20210410.gif"
        vipMapFour["(:20210411)"] = "file:///android_asset/miaohun/miaohun_20210411.gif"
        vipMapFour["(:20210412)"] = "file:///android_asset/miaohun/miaohun_20210412.gif"
        vipMapFour["(:20210413)"] = "file:///android_asset/miaohun/miaohun_20210413.gif"
        vipMapFour["(:20210414)"] = "file:///android_asset/miaohun/miaohun_20210414.gif"
        vipMapFour["(:20210415)"] = "file:///android_asset/miaohun/miaohun_20210415.gif"
        vipMapFour["(:20210416)"] = "file:///android_asset/miaohun/miaohun_20210416.gif"
        vipMapFour["(:20210417)"] = "file:///android_asset/miaohun/miaohun_20210417.gif"
        vipMapFour["(:20210418)"] = "file:///android_asset/miaohun/miaohun_20210418.gif"
        vipMapFour["(:20210419)"] = "file:///android_asset/miaohun/miaohun_20210419.gif"
        vipMapFour["(:20210420)"] = "file:///android_asset/miaohun/miaohun_20210420.gif"
        vipMapFour["(:20210421)"] = "file:///android_asset/miaohun/miaohun_20210421.gif"
        vipMapFour["(:20210422)"] = "file:///android_asset/miaohun/miaohun_20210422.gif"
        vipMapFour["(:20210423)"] = "file:///android_asset/miaohun/miaohun_20210423.gif"
        vipMapFour["(:20210424)"] = "file:///android_asset/miaohun/miaohun_20210424.gif"
        vipEmotionMap["喵魂"] = vipMapFour

        val vipMapFive: LinkedHashMap<String, String> = LinkedHashMap()
        vipMapFive["(:20210501)"] = "file:///android_asset/pilitu/pilitu_20210501.gif"
        vipMapFive["(:20210502)"] = "file:///android_asset/pilitu/pilitu_20210502.gif"
        vipMapFive["(:20210503)"] = "file:///android_asset/pilitu/pilitu_20210503.gif"
        vipMapFive["(:20210504)"] = "file:///android_asset/pilitu/pilitu_20210504.gif"
        vipMapFive["(:20210505)"] = "file:///android_asset/pilitu/pilitu_20210505.gif"
        vipMapFive["(:20210506)"] = "file:///android_asset/pilitu/pilitu_20210506.gif"
        vipMapFive["(:20210507)"] = "file:///android_asset/pilitu/pilitu_20210507.gif"
        vipMapFive["(:20210508)"] = "file:///android_asset/pilitu/pilitu_20210508.gif"
        vipMapFive["(:20210509)"] = "file:///android_asset/pilitu/pilitu_20210509.gif"
        vipMapFive["(:20210510)"] = "file:///android_asset/pilitu/pilitu_20210510.gif"
        vipMapFive["(:20210511)"] = "file:///android_asset/pilitu/pilitu_20210511.gif"
        vipMapFive["(:20210512)"] = "file:///android_asset/pilitu/pilitu_20210512.gif"
        vipMapFive["(:20210513)"] = "file:///android_asset/pilitu/pilitu_20210513.gif"
        vipMapFive["(:20210514)"] = "file:///android_asset/pilitu/pilitu_20210514.gif"
        vipMapFive["(:20210515)"] = "file:///android_asset/pilitu/pilitu_20210515.gif"
        vipMapFive["(:20210516)"] = "file:///android_asset/pilitu/pilitu_20210516.gif"
        vipEmotionMap["霹雳兔"] = vipMapFive

        val vipMapSix: LinkedHashMap<String, String> = LinkedHashMap()
        vipMapSix["(:20210601)"] = "file:///android_asset/qiuqiu/qiuqiu_20210601.gif"
        vipMapSix["(:20210602)"] = "file:///android_asset/qiuqiu/qiuqiu_20210602.gif"
        vipMapSix["(:20210603)"] = "file:///android_asset/qiuqiu/qiuqiu_20210603.gif"
        vipMapSix["(:20210604)"] = "file:///android_asset/qiuqiu/qiuqiu_20210604.gif"
        vipMapSix["(:20210605)"] = "file:///android_asset/qiuqiu/qiuqiu_20210605.gif"
        vipMapSix["(:20210606)"] = "file:///android_asset/qiuqiu/qiuqiu_20210606.gif"
        vipMapSix["(:20210607)"] = "file:///android_asset/qiuqiu/qiuqiu_20210607.gif"
        vipMapSix["(:20210608)"] = "file:///android_asset/qiuqiu/qiuqiu_20210608.gif"
        vipMapSix["(:20210609)"] = "file:///android_asset/qiuqiu/qiuqiu_20210609.gif"
        vipMapSix["(:20210610)"] = "file:///android_asset/qiuqiu/qiuqiu_20210610.gif"
        vipMapSix["(:20210611)"] = "file:///android_asset/qiuqiu/qiuqiu_20210611.gif"
        vipMapSix["(:20210612)"] = "file:///android_asset/qiuqiu/qiuqiu_20210612.gif"
        vipMapSix["(:20210613)"] = "file:///android_asset/qiuqiu/qiuqiu_20210613.gif"
        vipMapSix["(:20210614)"] = "file:///android_asset/qiuqiu/qiuqiu_20210614.gif"
        vipMapSix["(:20210615)"] = "file:///android_asset/qiuqiu/qiuqiu_20210615.gif"
        vipMapSix["(:20210616)"] = "file:///android_asset/qiuqiu/qiuqiu_20210616.gif"
        vipEmotionMap["球球"] = vipMapSix

        val vipMapSeven: LinkedHashMap<String, String> = LinkedHashMap()
        vipMapSeven["(:20210701)"] = "file:///android_asset/tianxintu/tianxintu_20210701.gif"
        vipMapSeven["(:20210702)"] = "file:///android_asset/tianxintu/tianxintu_20210702.gif"
        vipMapSeven["(:20210703)"] = "file:///android_asset/tianxintu/tianxintu_20210703.gif"
        vipMapSeven["(:20210704)"] = "file:///android_asset/tianxintu/tianxintu_20210704.gif"
        vipMapSeven["(:20210705)"] = "file:///android_asset/tianxintu/tianxintu_20210705.gif"
        vipMapSeven["(:20210706)"] = "file:///android_asset/tianxintu/tianxintu_20210706.gif"
        vipMapSeven["(:20210707)"] = "file:///android_asset/tianxintu/tianxintu_20210707.gif"
        vipMapSeven["(:20210708)"] = "file:///android_asset/tianxintu/tianxintu_20210708.gif"
        vipMapSeven["(:20210709)"] = "file:///android_asset/tianxintu/tianxintu_20210709.gif"
        vipMapSeven["(:20210710)"] = "file:///android_asset/tianxintu/tianxintu_20210710.gif"
        vipMapSeven["(:20210711)"] = "file:///android_asset/tianxintu/tianxintu_20210711.gif"
        vipMapSeven["(:20210712)"] = "file:///android_asset/tianxintu/tianxintu_20210712.gif"
        vipMapSeven["(:20210713)"] = "file:///android_asset/tianxintu/tianxintu_20210713.gif"
        vipMapSeven["(:20210714)"] = "file:///android_asset/tianxintu/tianxintu_20210714.gif"
        vipMapSeven["(:20210715)"] = "file:///android_asset/tianxintu/tianxintu_20210715.gif"
        vipMapSeven["(:20210716)"] = "file:///android_asset/tianxintu/tianxintu_20210716.gif"
        vipMapSeven["(:20210717)"] = "file:///android_asset/tianxintu/tianxintu_20210717.gif"
        vipMapSeven["(:20210718)"] = "file:///android_asset/tianxintu/tianxintu_20210718.gif"
        vipMapSeven["(:20210719)"] = "file:///android_asset/tianxintu/tianxintu_20210719.gif"
        vipMapSeven["(:20210720)"] = "file:///android_asset/tianxintu/tianxintu_20210720.gif"
        vipMapSeven["(:20210721)"] = "file:///android_asset/tianxintu/tianxintu_20210721.gif"
        vipMapSeven["(:20210722)"] = "file:///android_asset/tianxintu/tianxintu_20210722.gif"
        vipMapSeven["(:20210723)"] = "file:///android_asset/tianxintu/tianxintu_20210723.gif"
        vipMapSeven["(:20210724)"] = "file:///android_asset/tianxintu/tianxintu_20210724.gif"
        vipEmotionMap["甜心兔"] = vipMapSeven

        val vipMapEight: LinkedHashMap<String, String> = LinkedHashMap()
        vipMapEight["(:20210801)"] = "file:///android_asset/tubaibai/tubaibai_20210801.gif"
        vipMapEight["(:20210802)"] = "file:///android_asset/tubaibai/tubaibai_20210802.gif"
        vipMapEight["(:20210803)"] = "file:///android_asset/tubaibai/tubaibai_20210803.gif"
        vipMapEight["(:20210804)"] = "file:///android_asset/tubaibai/tubaibai_20210804.gif"
        vipMapEight["(:20210805)"] = "file:///android_asset/tubaibai/tubaibai_20210805.gif"
        vipMapEight["(:20210806)"] = "file:///android_asset/tubaibai/tubaibai_20210806.gif"
        vipMapEight["(:20210807)"] = "file:///android_asset/tubaibai/tubaibai_20210807.gif"
        vipMapEight["(:20210808)"] = "file:///android_asset/tubaibai/tubaibai_20210808.gif"
        vipMapEight["(:20210809)"] = "file:///android_asset/tubaibai/tubaibai_20210809.gif"
        vipEmotionMap["兔白白"] = vipMapEight
    }
}