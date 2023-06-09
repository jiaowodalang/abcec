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

package org.apache.druid.msq.input;

import com.google.common.base.Preconditions;
import org.apache.druid.frame.channel.ReadableFrameChannel;
import org.apache.druid.frame.read.FrameReader;
import org.apache.druid.java.util.common.ISE;
import org.apache.druid.msq.input.table.SegmentWithDescriptor;
import org.apache.druid.msq.kernel.StagePartition;

import javax.annotation.Nullable;

/**
 * A single item of readable input. Generated by {@link InputSliceReader#attach} from an {@link InputSlice}.
 *
 * Each item is either readable as a {@link org.apache.druid.segment.Segment} or as a {@link ReadableFrameChannel},
 * but not both. Check {@link #hasSegment()} and {@link #hasChannel()} to see which one you have.
 */
public class ReadableInput
{
  @Nullable
  private final SegmentWithDescriptor segment;

  @Nullable
  private final ReadableFrameChannel channel;

  @Nullable
  private final FrameReader frameReader;

  @Nullable
  private final StagePartition stagePartition;

  private ReadableInput(
      @Nullable SegmentWithDescriptor segment,
      @Nullable ReadableFrameChannel channel,
      @Nullable FrameReader frameReader,
      @Nullable StagePartition stagePartition
  )
  {
    this.segment = segment;
    this.channel = channel;
    this.frameReader = frameReader;
    this.stagePartition = stagePartition;

    if ((segment == null) == (channel == null)) {
      throw new ISE("Provide either 'segment' or 'channel'");
    }
  }

  /**
   * Create an input associated with a physical segment.
   *
   * @param segment the segment
   */
  public static ReadableInput segment(final SegmentWithDescriptor segment)
  {
    return new ReadableInput(Preconditions.checkNotNull(segment, "segment"), null, null, null);
  }

  /**
   * Create an input associated with a channel.
   *
   * @param channel        the channel
   * @param frameReader    reader for the channel
   * @param stagePartition stage-partition associated with the channel, if meaningful. May be null if this channel
   *                       does not correspond to any one particular stage-partition.
   */
  public static ReadableInput channel(
      final ReadableFrameChannel channel,
      final FrameReader frameReader,
      @Nullable final StagePartition stagePartition
  )
  {
    return new ReadableInput(
        null,
        Preconditions.checkNotNull(channel, "channel"),
        Preconditions.checkNotNull(frameReader, "frameReader"),
        stagePartition
    );
  }

  /**
   * Whether this input is a segment (from {@link #segment(SegmentWithDescriptor)}.
   */
  public boolean hasSegment()
  {
    return segment != null;
  }

  /**
   * Whether this input is a channel (from {@link #channel(ReadableFrameChannel, FrameReader, StagePartition)}.
   */
  public boolean hasChannel()
  {
    return channel != null;
  }

  /**
   * The segment for this input. Only valid if {@link #hasSegment()}.
   */
  public SegmentWithDescriptor getSegment()
  {
    checkIsSegment();
    return segment;
  }

  /**
   * The channel for this input. Only valid if {@link #hasChannel()}.
   */
  public ReadableFrameChannel getChannel()
  {
    checkIsChannel();
    return channel;
  }

  /**
   * The frame reader for this input. Only valid if {@link #hasChannel()}.
   */
  public FrameReader getChannelFrameReader()
  {
    checkIsChannel();
    return frameReader;
  }

  /**
   * The stage-partition this input. Only valid if {@link #hasChannel()}, and if a stage-partition was provided
   * during construction. Throws {@link IllegalStateException} if no stage-partition was provided during construction.
   */
  public StagePartition getStagePartition()
  {
    checkIsChannel();

    if (stagePartition == null) {
      throw new ISE("Stage-partition is not set for this channel");
    }

    return stagePartition;
  }

  private void checkIsSegment()
  {
    if (!hasSegment()) {
      throw new ISE("Not a channel input; cannot call this method");
    }
  }

  private void checkIsChannel()
  {
    if (!hasChannel()) {
      throw new ISE("Not a channel input; cannot call this method");
    }
  }
}
