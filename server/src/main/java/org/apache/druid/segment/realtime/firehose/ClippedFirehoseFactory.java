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

package org.apache.druid.segment.realtime.firehose;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Predicate;
import org.apache.druid.data.input.Firehose;
import org.apache.druid.data.input.FirehoseFactory;
import org.apache.druid.data.input.InputRow;
import org.apache.druid.data.input.impl.InputRowParser;
import org.joda.time.Interval;

import java.io.File;
import java.io.IOException;

/**
 * Creates firehoses clipped to a particular time interval. Useful for enforcing min time, max time, and time windows.
 */
@Deprecated
public class ClippedFirehoseFactory implements FirehoseFactory
{
  private final FirehoseFactory delegate;
  private final Interval interval;

  @JsonCreator
  public ClippedFirehoseFactory(
      @JsonProperty("delegate") FirehoseFactory delegate,
      @JsonProperty("interval") Interval interval
  )
  {
    this.delegate = delegate;
    this.interval = interval;
  }

  @JsonProperty
  public FirehoseFactory getDelegate()
  {
    return delegate;
  }

  @JsonProperty
  public Interval getInterval()
  {
    return interval;
  }

  @Override
  public Firehose connect(InputRowParser parser, File temporaryDirectory) throws IOException
  {
    return new PredicateFirehose(
        delegate.connect(parser, temporaryDirectory),
        new Predicate<InputRow>()
        {
          @Override
          public boolean apply(InputRow input)
          {
            return interval.contains(input.getTimestampFromEpoch());
          }
        }
    );
  }

}
