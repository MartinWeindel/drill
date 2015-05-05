/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.drill.exec.sql;

import com.google.common.collect.ImmutableList;
import org.apache.drill.BaseTestQuery;
import org.junit.Test;

public class TestCTAS extends BaseTestQuery {
  @Test // DRILL-2589
  public void withDuplicateColumnsInDef1() throws Exception {
    ctasErrorTestHelper("CREATE TABLE %s.%s AS SELECT region_id, region_id FROM cp.`region.json`",
        String.format("Error: Duplicate column name [%s]", "region_id")
    );
  }

  @Test // DRILL-2589
  public void withDuplicateColumnsInDef2() throws Exception {
    ctasErrorTestHelper("CREATE TABLE %s.%s AS SELECT region_id, sales_city, sales_city FROM cp.`region.json`",
        String.format("Error: Duplicate column name [%s]", "sales_city")
    );
  }

  @Test // DRILL-2589
  public void withDuplicateColumnsInDef3() throws Exception {
    ctasErrorTestHelper(
        "CREATE TABLE %s.%s(regionid, regionid) " +
            "AS SELECT region_id, sales_city FROM cp.`region.json`",
        String.format("Error: Duplicate column name [%s]", "regionid")
    );
  }

  @Test // DRILL-2589
  public void withDuplicateColumnsInDef4() throws Exception {
    ctasErrorTestHelper(
        "CREATE TABLE %s.%s(regionid, salescity, salescity) " +
            "AS SELECT region_id, sales_city, sales_city FROM cp.`region.json`",
        String.format("Error: Duplicate column name [%s]", "salescity")
    );
  }

  @Test // DRILL-2589
  public void withDuplicateColumnsInDef5() throws Exception {
    ctasErrorTestHelper(
        "CREATE TABLE %s.%s(regionid, salescity, SalesCity) " +
            "AS SELECT region_id, sales_city, sales_city FROM cp.`region.json`",
        String.format("Error: Duplicate column name [%s]", "SalesCity")
    );
  }

  @Test // DRILL-2589
  public void whenInEqualColumnCountInTableDefVsInTableQuery() throws Exception {
    ctasErrorTestHelper(
        "CREATE TABLE %s.%s(regionid, salescity) " +
            "AS SELECT region_id, sales_city, sales_region FROM cp.`region.json`",
        "Error: table's field list and the table's query field list have different counts."
    );
  }

  @Test // DRILL-2589
  public void whenTableQueryColumnHasStarAndTableFiledListIsSpecified() throws Exception {
    ctasErrorTestHelper(
        "CREATE TABLE %s.%s(regionid, salescity) " +
            "AS SELECT region_id, * FROM cp.`region.json`",
        "Error: table's query field list has a '*', which is invalid when table's field list is specified."
    );
  }

  private static void ctasErrorTestHelper(final String ctasSql, final String errorMsg)
      throws Exception {
    final String createTableSql = String.format(ctasSql, "dfs_test.tmp", "testTableName");

    testBuilder()
        .sqlQuery(createTableSql)
        .unOrdered()
        .baselineColumns("ok", "summary")
        .baselineValues(false, errorMsg)
        .go();
  }
}
