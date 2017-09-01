## CODI JobService

### Overview
- 定时任务


### Install
- 安装`sql/tables_mysql_innodb.sql`


### Job List

> 科地定时任务组和定时触发器组，分别为`CODI_JOBS`,`CODI_TRIGGERS`

<table>
  <thead>
    <tr>
    <td>Job Name</td>
    <td>Trigger Time</td>
    <td>Desc</td>
    </tr>
  </thead>
  <tbody>
  <tr>
      <td>SH300_ALL_JOB</td>
      <td>手动执行</td>
      <td>深沪300统计数据（全量，从2015年起）</td>
    </tr>
    <tr>
      <td>SH300_LATEST_JOB</td>
      <td>每天1:00</td>
      <td>获取最新的深沪300数据（增量）</td>
    </tr>
    <tr>
        <td>UPDATE_FUNDINFO_JOB</td>
        <td>每天1：30</td>
        <td>基金在售列表</td>
    </tr>
    <tr>
      <td>SECU_MAIN_JOB</td>
      <td>每周日2：00</td>
      <td>刷新证券产品任务</td>
    </tr>
    <tr>
      <td>FUND_TYPE_JOB</td>
      <td>每天2：00</td>
      <td>刷新基金类型任务</td>
    </tr>
    <tr>
      <td>FUND_RATING_JOB</td>
      <td>每周1,3的2：00</td>
      <td>基金评级</td>
    </tr>
    <tr>
      <td>FUND_SORT_JOB</td>
      <td>每天2：00</td>
      <td>基金排序（全量、日、周、三月、半年、年）</td>
    </tr>
    <tr>
      <td>FUND_CODE_RELATIONSHIP_JOB</td>
      <td>每天2：10</td>
      <td>刷新基金编码关联任务</td>
    </tr>
    
  </tbody>
</table>

### Execute Job
- `http://x.x.x.x/codi-jobservice/quartz/execute?jobName={JOB_NAME}&groupName=CODI_JOBS`


### 参考资料
- <http://www.quartz-scheduler.org/overview/features.html>
- <http://docs.spring.io/spring/docs/current/spring-framework-reference/html/scheduling.html>
