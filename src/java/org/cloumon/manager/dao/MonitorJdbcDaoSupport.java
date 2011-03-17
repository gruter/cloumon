package org.cloumon.manager.dao;

import javax.annotation.Resource;

import org.springframework.jdbc.core.JdbcTemplate;

public abstract class MonitorJdbcDaoSupport {
  @Resource(name="jdbcTemplate")
  protected JdbcTemplate jdbcTemplate;
  
  protected JdbcTemplate getJdbcTemplate() {
    return jdbcTemplate;
  }
}
