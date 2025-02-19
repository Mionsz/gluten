/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.glutenproject.substrait.rel;

import io.glutenproject.substrait.expression.AggregateFunctionNode;
import io.glutenproject.substrait.expression.ExpressionNode;
import io.glutenproject.substrait.extensions.AdvancedExtensionNode;
import io.substrait.proto.AggregateRel;
import io.substrait.proto.Rel;
import io.substrait.proto.RelCommon;

import java.io.Serializable;
import java.util.ArrayList;

public class AggregateRelNode implements RelNode, Serializable {
  private final RelNode input;
  private final ArrayList<ExpressionNode> groupings = new ArrayList<>();
  private final ArrayList<AggregateFunctionNode> aggregateFunctionNodes = new ArrayList<>();

  private final ArrayList<ExpressionNode> filters = new ArrayList<>();
  private final AdvancedExtensionNode extensionNode;

  AggregateRelNode(RelNode input,
                   ArrayList<ExpressionNode> groupings,
                   ArrayList<AggregateFunctionNode> aggregateFunctionNodes,
                   ArrayList<ExpressionNode> filters) {
    this.input = input;
    this.groupings.addAll(groupings);
    this.aggregateFunctionNodes.addAll(aggregateFunctionNodes);
    this.filters.addAll(filters);
    this.extensionNode = null;
  }

  AggregateRelNode(RelNode input,
                   ArrayList<ExpressionNode> groupings,
                   ArrayList<AggregateFunctionNode> aggregateFunctionNodes,
                   ArrayList<ExpressionNode> filters,
                   AdvancedExtensionNode extensionNode) {
    this.input = input;
    this.groupings.addAll(groupings);
    this.aggregateFunctionNodes.addAll(aggregateFunctionNodes);
    this.filters.addAll(filters);
    this.extensionNode = extensionNode;
  }

  @Override
  public Rel toProtobuf() {
    RelCommon.Builder relCommonBuilder = RelCommon.newBuilder();
    relCommonBuilder.setDirect(RelCommon.Direct.newBuilder());

    AggregateRel.Grouping.Builder groupingBuilder =
        AggregateRel.Grouping.newBuilder();
    for (ExpressionNode exprNode : groupings) {
      groupingBuilder.addGroupingExpressions(exprNode.toProtobuf());
    }

    AggregateRel.Builder aggBuilder = AggregateRel.newBuilder();
    aggBuilder.setCommon(relCommonBuilder.build());
    aggBuilder.addGroupings(groupingBuilder.build());

    for (int i = 0; i < aggregateFunctionNodes.size(); i ++) {
      AggregateRel.Measure.Builder measureBuilder = AggregateRel.Measure.newBuilder();
      measureBuilder.setMeasure(aggregateFunctionNodes.get(i).toProtobuf());
      // Need to set the filter expression if exists filter expression.
      if (i < filters.size()) {
        measureBuilder.setFilter(filters.get(i).toProtobuf());
      }
      aggBuilder.addMeasures(measureBuilder.build());
    }

    if (input != null) {
      aggBuilder.setInput(input.toProtobuf());
    }
    if (extensionNode != null) {
      aggBuilder.setAdvancedExtension(extensionNode.toProtobuf());
    }
    Rel.Builder builder = Rel.newBuilder();
    builder.setAggregate(aggBuilder.build());
    return builder.build();
  }
}
