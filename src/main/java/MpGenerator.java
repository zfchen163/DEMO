
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.FileOutConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.TemplateConfig;
import com.baomidou.mybatisplus.generator.config.converts.MySqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.po.TableInfo;
import com.baomidou.mybatisplus.generator.config.rules.IColumnType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MpGenerator {
  //前端首页生成地址
  private static final String DISK_HTML = "D://test//html//";
  //后端控制器以及服务生成地址
  private static final String DISK_JAVA = "D://test//";
  private static final String AUTHOR = "赵铁锤";

  /**
   * <p>
   * MySQL 生成演示
   * </p>
   */
  public static void main(String[] args) {
    final AutoGenerator mpg = new AutoGenerator();
    // 选择 freemarker 引擎，默认 Veloctiy
    // mpg.setTemplateEngine(new FreemarkerTemplateEngine());
    final String[] include = {"account"};
    // 全局配置
    final GlobalConfig gc = new GlobalConfig();
    gc.setOutputDir(DISK_JAVA);
    gc.setFileOverride(true);
    gc.setActiveRecord(true);// 不需要ActiveRecord特性的请改为false
    gc.setEnableCache(false);// XML 二级缓存
    gc.setBaseResultMap(true);// XML ResultMap
    gc.setBaseColumnList(true);// XML columList
    //gc.setKotlin(true);//是否生成 kotlin 代码
    gc.setAuthor(AUTHOR);

    // 自定义文件命名，注意 %s 会自动填充表实体属性！
    // gc.setMapperName("%sDao");
    // gc.setXmlName("%sDao");
    // gc.setServiceName("MP%sService");
    // gc.setServiceImplName("%sServiceDiy");
    // gc.setControllerName("%sAction");
    mpg.setGlobalConfig(gc);

    // 数据源配置
    DataSourceConfig dsc = new DataSourceConfig();
    dsc.setDbType(DbType.MYSQL);
    dsc.setTypeConvert(new MySqlTypeConvert() {
      // 自定义数据库表字段类型转换【可选】
      public IColumnType processTypeConvert(String fieldType) {
        System.out.println("转换类型：" + fieldType);
        // 注意！！processTypeConvert 存在默认类型转换，如果不是你要的效果请自定义返回、非如下直接返回。

        return super.processTypeConvert(gc, fieldType);
      }
    });
    dsc.setDriverName("com.mysql.jdbc.Driver");
    dsc.setUsername("root");
    dsc.setPassword("root");
    dsc.setUrl("jdbc:mysql://127.0.0.1:3306/biz?characterEncoding=utf8");
    mpg.setDataSource(dsc);

    // 策略配置
    StrategyConfig strategy = new StrategyConfig();
    // strategy.setCapitalMode(true);// 全局大写命名 ORACLE 注意
    //strategy.setTablePrefix(new String[] { "tlog_", "tsys_" });// 此处可以修改为您的表前缀
    strategy.setNaming(NamingStrategy.underline_to_camel);// 表名生成策略

    strategy.setInclude(include); // 需要生成的表
    // strategy.setExclude(new String[]{"test"}); // 排除生成的表
    // 自定义实体父类
    // strategy.setSuperEntityClass("com.baomidou.demo.TestEntity");
    // 自定义实体，公共字段
    // strategy.setSuperEntityColumns(new String[] { "test_id", "age" });
    // 自定义 mapper 父类
    // strategy.setSuperMapperClass("com.baomidou.demo.TestMapper");
    // 自定义 service 父类
    // strategy.setSuperServiceClass("com.baomidou.demo.TestService");
    // 自定义 service 实现类父类
    // strategy.setSuperServiceImplClass("com.baomidou.demo.TestServiceImpl");
    // 自定义 controller 父类
    // strategy.setSuperControllerClass("com.baomidou.demo.TestController");
    // 【实体】是否生成字段常量（默认 false）
    // public static final String ID = "test_id";
    strategy.setEntityColumnConstant(true);
    // 【实体】是否为构建者模型（默认 false）
    // public User setName(String name) {this.name = name; return this;}
    //strategy.setEntityBuilderModel(true);
    mpg.setStrategy(strategy);

    // 包配置
    PackageConfig pc = new PackageConfig();
    pc.setParent("com.pipi");
    pc.setController("controller");
    pc.setEntity("model");
    mpg.setPackageInfo(pc);

    // 注入自定义配置，可以在 VM 中使用 cfg.abc 【可无】  ${cfg.abc}
    InjectionConfig cfg = new InjectionConfig() {
      @Override
      public void initMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("abc", this.getConfig().getGlobalConfig().getAuthor() + "-mp");
        this.setMap(map);
      }
    };

    // 自定义 xxListIndex.html 生成
    List<FileOutConfig> focList = new ArrayList<FileOutConfig>();
    focList.add(new FileOutConfig("/templates/templatesMybatis/list.html.vm") {
      @Override
      public String outputFile(TableInfo tableInfo) {
        // 自定义输入文件名称
        return DISK_HTML + tableInfo.getEntityName() + "ListIndex.html";
      }
    });
    cfg.setFileOutConfigList(focList);
    mpg.setCfg(cfg);

    // 自定义  xxAdd.html 生成
    focList.add(new FileOutConfig("/templates/templatesMybatis/add.html.vm") {
      @Override
      public String outputFile(TableInfo tableInfo) {
        // 自定义输入文件名称
        return DISK_HTML + tableInfo.getEntityName() + "Add.html";
      }
    });
    cfg.setFileOutConfigList(focList);
    mpg.setCfg(cfg);

    //  自定义 xxUpdate.html生成
    focList.add(new FileOutConfig("/templates/templatesMybatis/update.html.vm") {
      @Override
      public String outputFile(TableInfo tableInfo) {
        // 自定义输入文件名称
        return DISK_HTML + tableInfo.getEntityName() + "Update.html";
      }
    });
    cfg.setFileOutConfigList(focList);
    mpg.setCfg(cfg);

    // 关闭默认 xml 生成，调整生成 至 根目录
        /*TemplateConfig tc = new TemplateConfig();
        tc.setXml(null);
        mpg.setTemplate(tc);*/

    // 自定义模板配置，可以 copy 源码 mybatis-plus/src/main/resources/templates 下面内容修改，
    // 放置自己项目的 src/main/resources/templates 目录下, 默认名称一下可以不配置，也可以自定义模板名称
    TemplateConfig tc = new TemplateConfig();
    tc.setController("/templates/templatesMybatis/controller.java.vm");
    tc.setService("/templates/templatesMybatis/service.java.vm");
    tc.setServiceImpl("/templates/templatesMybatis/serviceImpl.java.vm");
    tc.setEntity("/templates/templatesMybatis/entity.java.vm");
    tc.setMapper("/templates/templatesMybatis/mapper.java.vm");
    tc.setXml("/templates/templatesMybatis/mapper.xml.vm");

    // 如上任何一个模块如果设置 空 OR Null 将不生成该模块。
    mpg.setTemplate(tc);

    // 执行生成
    mpg.execute();

    // 打印注入设置【可无】
    System.err.println(mpg.getCfg().getMap().get("abc"));
  }


}
