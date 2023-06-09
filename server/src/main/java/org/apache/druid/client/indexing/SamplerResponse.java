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

package org.apache.druid.client.indexing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.druid.data.input.impl.DimensionSchema;
import org.apache.druid.segment.column.RowSignature;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SamplerResponse
{
  private final int numRowsRead;
  private final int numRowsIndexed;

  private final List<DimensionSchema> logicalDimensions;
  private final List<DimensionSchema> physicalDimensions;
  private final RowSignature logicalSegmentSchema;
  private final List<SamplerResponseRow> data;

  @JsonCreator
  public SamplerResponse(
      @JsonProperty("numRowsRead") int numRowsRead,
      @JsonProperty("numRowsIndexed") int numRowsIndexed,
      @JsonProperty("logicalDimensions") List<DimensionSchema> logicalDimensions,
      @JsonProperty("physicalDimensions") List<DimensionSchema> physicalDimensions,
      @JsonProperty("logicalSegmentSchema") RowSignature logicalSegmentSchema,
      @JsonProperty("data") List<SamplerResponseRow> data
  )
  {
    this.numRowsRead = numRowsRead;
    this.numRowsIndexed = numRowsIndexed;
    this.logicalDimensions = logicalDimensions;
    this.physicalDimensions = physicalDimensions;
    this.logicalSegmentSchema = logicalSegmentSchema;
    this.data = data;
  }

  @JsonProperty
  public int getNumRowsRead()
  {
    return numRowsRead;
  }

  @JsonProperty
  public int getNumRowsIndexed()
  {
    return numRowsIndexed;
  }

  @JsonProperty
  public List<DimensionSchema> getLogicalDimensions()
  {
    return logicalDimensions;
  }

  @JsonProperty
  public List<DimensionSchema> getPhysicalDimensions()
  {
    return physicalDimensions;
  }

  @JsonProperty
  public RowSignature getLogicalSegmentSchema()
  {
    return logicalSegmentSchema;
  }

  @JsonProperty
  public List<SamplerResponseRow> getData()
  {
    return data;
  }

  @Override
  public boolean equals(Object o)
  {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SamplerResponse that = (SamplerResponse) o;
    return getNumRowsRead() == that.getNumRowsRead() &&
           getNumRowsIndexed() == that.getNumRowsIndexed() &&
           Objects.equals(logicalDimensions, that.logicalDimensions) &&
           Objects.equals(physicalDimensions, that.physicalDimensions) &&
           Objects.equals(logicalSegmentSchema, that.logicalSegmentSchema) &&
           Objects.equals(data, that.data);
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(
        getNumRowsRead(),
        getNumRowsIndexed(),
        logicalDimensions,
        physicalDimensions,
        logicalSegmentSchema,
        data
    );
  }

  @Override
  public String toString()
  {
    return "SamplerResponse{" +
           "numRowsRead=" + numRowsRead +
           ", numRowsIndexed=" + numRowsIndexed +
           ", logicalDimensions=" + logicalDimensions +
           ", physicalDimensions=" + physicalDimensions +
           ", logicalSegmentSchema=" + logicalSegmentSchema +
           ", data=" + data +
           '}';
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class SamplerResponseRow
  {
    private final Map<String, Object> input;
    private final Map<String, Object> parsed;
    private final Boolean unparseable;
    private final String error;

    @JsonCreator
    public SamplerResponseRow(
        @JsonProperty("input") Map<String, Object> input,
        @JsonProperty("parsed") Map<String, Object> parsed,
        @JsonProperty("unparseable") Boolean unparseable,
        @JsonProperty("error") String error
    )
    {
      this.input = input;
      this.parsed = parsed;
      this.unparseable = unparseable;
      this.error = error;
    }

    @JsonProperty
    public Map<String, Object> getInput()
    {
      return input;
    }

    @JsonProperty
    public Map<String, Object> getParsed()
    {
      return parsed;
    }

    @JsonProperty
    public Boolean isUnparseable()
    {
      return unparseable;
    }

    @JsonProperty
    public String getError()
    {
      return error;
    }

    public SamplerResponseRow withParsed(Map<String, Object> parsed)
    {
      return new SamplerResponseRow(input, parsed, unparseable, error);
    }

    @Override
    public boolean equals(Object o)
    {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      SamplerResponseRow that = (SamplerResponseRow) o;
      return Objects.equals(input, that.input) &&
             Objects.equals(parsed, that.parsed) &&
             Objects.equals(unparseable, that.unparseable) &&
             Objects.equals(error, that.error);
    }

    @Override
    public int hashCode()
    {
      return Objects.hash(input, parsed, unparseable, error);
    }

    @Override
    public String toString()
    {
      return "SamplerResponseRow{" +
             "rawInput=" + input +
             ", parsed=" + parsed +
             ", unparseable=" + unparseable +
             ", error='" + error + '\'' +
             '}';
    }
  }
}
