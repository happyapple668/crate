/*
 * Licensed to Crate under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.  Crate licenses this file
 * to you under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial
 * agreement.
 */

package io.crate.analyze;

import io.crate.auth.user.User;
import io.crate.exceptions.RelationUnknown;
import io.crate.metadata.RelationName;
import io.crate.metadata.Schemas;
import io.crate.metadata.blob.BlobSchemaInfo;
import io.crate.sql.tree.DropBlobTable;
import io.crate.sql.tree.QualifiedName;
import io.crate.sql.tree.Table;
import io.crate.test.integration.CrateUnitTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

public class DropBlobTableAnalyzerTest extends CrateUnitTest {

    private static final String IRRELEVANT = "Irrelevant";
    private RelationName relationName = new RelationName(BlobSchemaInfo.NAME, IRRELEVANT);
    private Table table = new Table(new QualifiedName(IRRELEVANT));

    @Mock
    public Schemas schemas;
    private DropBlobTableAnalyzer analyzer;

    @Before
    public void setUpAnalyzer() throws Exception {
        analyzer = new DropBlobTableAnalyzer(schemas);
    }

    @Test
    public void testDeletingNoExistingTableSetsNoopIfIgnoreNonExistentTablesIsSet() throws Exception {
        when(schemas.getTableInfo(User.CRATE_USER, relationName)).thenThrow(new RelationUnknown(relationName));

        DropBlobTableAnalyzedStatement statement = analyzer.analyze(User.CRATE_USER, new DropBlobTable(table, true));
        assertThat(statement.noop(), is(true));
    }

    @Test
    public void testDeletingNonExistingTableRaisesException() throws Exception {
        when(schemas.getTableInfo(User.CRATE_USER, relationName)).thenThrow(new RelationUnknown(relationName));

        expectedException.expect(RelationUnknown.class);
        expectedException.expectMessage("Relation 'blob.Irrelevant' unknown");
        analyzer.analyze(User.CRATE_USER, new DropBlobTable(table, false));
    }
}
