# 不兼容变更
* 2.0.0-beta4
  * `HttpUtil`删除`toUrlParams`、`toMapParams`方法。
* 2.0.0-beta2
  * `ContentTypeConst`、`HeaderConst`、`ReqMethodConst`移至`http.constant`包下。
  * `NanoIdUtil`等 ID 工具类移至`id`包下。
* 2.0.0-alpha5
  * 大幅修改`RegExUtil`中的匹配、替换方法。
  * 删除`FlagsEnum`和`RegExUtil`中的相关方法。
* 2.0.0-alpha1
  * 分模块。
* 1.14.1
  * 删除`crypto.SecurityUtil`。
* 1.14.0
  * `RegExUtils`删除有 Pattern 形参的方法。
  * 所有类名含`Utils`的重命名为`Util`，含`String`的重命名为`Str`，含`Collection`的重命名为`Coll`，含`Feature`的重命名为`Feat`，含`Constant`的重命名为`Const`，含`Property`的重命名为`Prop`，含`Function`的重命名为`Func`。
  * `date.constant.DateRegexPattern`重命名为`DateRegExPattern`。
  * `ArrayUtil`、`ClassUtil`、`NumberUtil`、`ObjUtil`、`RandomStrUtil`、`RandomUtil`、`StrUtil`、`SysUtil`移至`lang`包下，`CollUtil`、`MapUtil`移至`coll`包下，`JsonUtil`移至`json`包下，`XmlUtil`移至`xml`包下，`PropFunc`移至`bean`包下。
* 1.13.5
  * `HttpUtils`返回`HttpResult.Body`的方法改为返回`HttpResult`。
* 1.13.2
  * `DateFormat`的 final 属性名后缀由国家替换为语言。
* 1.13.1
  * `DateUtils`的`getFormatterBuilder`方法改为`private`。
* 1.13.0
  * `YamlUtils`移至`yaml`包下。
* 1.12.0
  * `ClassUtils.isBasic`重命名为`isPrimitiveType`。
  * `BeanUtils`移至`bean`包下。
  * `BeanUtils.FieldFunction`重命名为`PropertyFunction`并移至根包下。
  * `BeanUtils.getFieldName(FieldFunction)`重命名为`getPropertyName(PropertyFunction)`。
  * 删除`BeanUtils.deepToMap`。
* 1.11.0
  * Group Id 由`top.zhogjianhao`重命名为`top.csaf`。
* 1.10.1
  * `id.NanoIDUtils`重命名为`NanoIdUtils`。
* 1.9.2
  * 删除`FileUtils.getClassRootPath`；
  * 删除`FileUtils.getClassPath`；
  * `FileUtils`移至`io`包下。
* 1.8.1
  * `date.DateFeature`中静态变量的赋值方式由直接赋值改为通过`set`、`setAlways`方法赋值；
  * `DateUtils.defaultLocalDatePattern`移至`DateConstant.DEFAULT_LOCAL_DATE_PATTERN`；
  * `DateUtils.defaultLocalDateTimePattern`移至`DateConstant.DEFAULT_LOCAL_DATE_TIME_PATTERN`；
  * `DateUtils.defaultLocalTimePattern`移至`DateConstant.DEFAULT_LOCAL_TIME_PATTERN`。
* 1.8.0
  * `date.DateDuration`移至`date.constant`包下；
  * `date.DatePattern`移至`date.constant`包下；
  * `date.DatePatternRegex`移至`date.constant.DateRegexPattern` ；
  * `constant.CommonPatternConstant`重命名为`PatternConstant`;
  * `RegExUtils`移至`regex`包下。
* 1.7.0
  * `JsonUtils`的`toJson(Object, SerializerFeature...)`、`parseObject(String, Class<T>, Feature...)`、`parseObject(String, Class<T>, ParserConfig)`方法，SerializerFeature... 替换为 **JSONWriter.Feature...**，Feature... 和 ParserConfig 替换为 **JSONReader.Feature...**。
* 1.5.1
  * 删除`ArrayUtils#indexOf(String[], String, int)`。
* 1.2.4
  * `DateUtils`移至`date`包下。
* 1.2.0
  * `CollectionUtils`的`moveForward、remove`方法，移至`ArrayUtils`中并重构优化。
