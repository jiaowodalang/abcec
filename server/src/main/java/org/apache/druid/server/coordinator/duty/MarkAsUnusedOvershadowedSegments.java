/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.druid.server.coordinator.duty;

import org.apache.druid.client.ImmutableDruidDataSource;
import org.apache.druid.client.ImmutableDruidServer;
import org.apache.druid.java.util.common.logger.Logger;
import org.apache.druid.server.coordinator.CoordinatorStats;
import org.apache.druid.server.coordinator.DruidCluster;
import org.apache.druid.server.coordinator.DruidCoordinator;
import org.apache.druid.server.coordinator.DruidCoordinatorRuntimeParams;
import org.apache.druid.server.coordinator.ServerHolder;
import org.apache.druid.timeline.DataSegment;
import org.apache.druid.timeline.SegmentId;
import org.apache.druid.timeline.SegmentTimeline;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

public class MarkAsUnusedOvershadowedSegments implements CoordinatorDuty
{
  private static final Logger log = new Logger(MarkAsUnusedOvershadowedSegments.class);

  private final DruidCoordinator coordinator;

  public MarkAsUnusedOvershadowedSegments(DruidCoordinator coordinator)
  {
    this.coordinator = coordinator;
  }

  @Override
  public DruidCoordinatorRuntimeParams run(DruidCoordinatorRuntimeParams params)
  {
    // Mark as unused overshadowed segments only if we've had enough time to make sure we aren't flapping with old data.
    if (!params.coordinatorIsLeadingEnoughTimeToMarkAsUnusedOvershadowedSegements()) {
      log.info("Skipping MarkAsUnused as coordinator is not leading enough time.");
      return params;
    }

    final Set<DataSegment> allOvershadowedSegments = params.getDataSourcesSnapshot().getOvershadowedSegments();
    if (allOvershadowedSegments.isEmpty()) {
      log.info("Skipping MarkAsUnused as there are no overshadowed segments.");
      return params;
    }

    CoordinatorStats stats = new CoordinatorStats();

    DruidCluster cluster = params.getDruidCluster();
    final Map<String, SegmentTimeline> timelines = new HashMap<>();

    for (SortedSet<ServerHolder> serverHolders : cluster.getSortedHistoricalsByTier()) {
      for (ServerHolder serverHolder : serverHolders) {
        addSegmentsFromServer(serverHolder, timelines);
      }
    }

    for (ServerHolder serverHolder : cluster.getBrokers()) {
      addSegmentsFromServer(serverHolder, timelines);
    }

    // Note that we do not include segments from ingestion services such as tasks or indexers,
    // to prevent unpublished segments from prematurely overshadowing segments.

    // Mark all segments as unused in db that are overshadowed by served segments
    final Map<String, Set<SegmentId>> datasourceToUnusedSegments = new HashMap<>();
    for (DataSegment dataSegment : allOvershadowedSegments) {
      SegmentTimeline timeline = timelines.get(dataSegment.getDataSource());
      if (timeline != null && timeline.isOvershadowed(dataSegment)) {
        datasourceToUnusedSegments.computeIfAbsent(dataSegment.getDataSource(), ds -> new HashSet<>())
                                  .add(dataSegment.getId());
        stats.addToGlobalStat("overShadowedCount", 1);
      }
    }
    datasourceToUnusedSegments.forEach(coordinator::markSegmentsAsUnused);

    return params.buildFromExisting().withCoordinatorStats(stats).build();
  }

  private void addSegmentsFromServer(
      ServerHolder serverHolder,
      Map<String, SegmentTimeline> timelines
  )
  {
    ImmutableDruidServer server = serverHolder.getServer();

    for (ImmutableDruidDataSource dataSource : server.getDataSources()) {
      SegmentTimeline timeline = timelines
          .computeIfAbsent(dataSource.getName(), dsName -> new SegmentTimeline());
      timeline.addSegments(dataSource.getSegments().iterator());
    }
  }
}
