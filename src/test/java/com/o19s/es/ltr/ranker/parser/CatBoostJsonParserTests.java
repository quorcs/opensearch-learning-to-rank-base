/*
 * Copyright [2017] Wikimedia Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.o19s.es.ltr.ranker.parser;

import com.o19s.es.ltr.feature.FeatureSet;
import com.o19s.es.ltr.feature.store.StoredFeature;
import com.o19s.es.ltr.feature.store.StoredFeatureSet;
import com.o19s.es.ltr.ranker.DenseFeatureVector;
import com.o19s.es.ltr.ranker.catboost.CatBoostRanker;
import com.o19s.es.ltr.ranker.linear.LinearRankerTests;
import org.apache.lucene.tests.util.LuceneTestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.o19s.es.ltr.LtrTestUtils.randomFeature;

public class CatBoostJsonParserTests extends LuceneTestCase {
    private final CatBoostJsonParser parser = new CatBoostJsonParser();

    public void testReadSimpleSplit() throws IOException {
        String model = readModel("models/catboost.json");

        FeatureSet set = new StoredFeatureSet("set", features());
        CatBoostRanker tree = parser.parse(set, model);
        DenseFeatureVector v = tree.newFeatureVector(null);
        LinearRankerTests.fillRandomWeights(v.scores);
        tree.score(v);
        features();
    }

    private String readModel(String model) throws IOException {
        try (InputStream is = ClassLoader.getSystemResourceAsStream(model)) {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            is.transferTo(bos);
            return bos.toString(StandardCharsets.UTF_8);
        }
    }

    private List<StoredFeature> features() throws IOException {
        return List.of(
                randomFeature("duration"),
                randomFeature("category_id"),
                randomFeature("has_show_id"),
                randomFeature("created_decay_days"),
                randomFeature("views_last_day"),
                randomFeature("views_last_7_day"),
                randomFeature("views_last_30_day"),
                randomFeature("views_total"),
                randomFeature("views_last_day_rel_total"),
                randomFeature("views_last_7_day_rel_total"),
                randomFeature("views_last_30_day_rel_total"),
                randomFeature("mean_watch_time_last_day"),
                randomFeature("mean_day_views"),
                randomFeature("reactions_last_day"),
                randomFeature("for_distribution"),
                randomFeature("views_last_day_author"),
                randomFeature("views_last_7_day_author"),
                randomFeature("views_last_30_day_author"),
                randomFeature("subs_counter"),
                randomFeature("strict-match-title-feature"),
                randomFeature("strict-match-author-feature"),
                randomFeature("phrase-match-title-feature"),
                randomFeature("phrase-match-author-feature"),
                randomFeature("match-title-feature"),
                randomFeature("match-author-feature"),
                randomFeature("intervals-title-feature"),
                randomFeature("intervals-author-feature"),
                randomFeature("query_len"),
                randomFeature("title_len"),
                randomFeature("author_len")
        );
    }
}