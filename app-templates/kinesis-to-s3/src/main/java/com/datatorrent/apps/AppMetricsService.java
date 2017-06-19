package com.datatorrent.apps;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

import com.datatorrent.metrics.appmetrics.AppMetricComputeService.DefaultAppMetricComputeService;

public class AppMetricsService extends DefaultAppMetricComputeService
{
  private final Logger LOG = LoggerFactory.getLogger(AppMetricsService.class);
  @Override
  public Map<String, Object> computeAppLevelMetrics(Map<String, Map<String, Object>> completedMetrics)
  {

    Map<String, Object> output = Maps.newHashMap();
    return output;
  }


  private static final long serialVersionUID = 5119330693347067792L;


}
