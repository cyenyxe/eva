/*
 * European Variation Archive (EVA) - Open-access database of all types of genetic
 * variation data from all species
 *
 * Copyright 2014-2016 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.eva.server;

import org.apache.commons.lang.StringUtils;
import org.opencb.biodata.models.variant.VariantSource;
import org.opencb.datastore.core.QueryOptions;
import org.opencb.datastore.core.QueryResult;
import org.springframework.data.domain.PageRequest;

import uk.ac.ebi.eva.commons.models.data.VariantSourceEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Utils {

    private static Map<String, String> apiToMongoDocNameMap;

    static {
        apiToMongoDocNameMap = initApiToMongoDocNameMap();
    }

    public static Map<String, String> getApiToMongoDocNameMap() {
        return Collections.unmodifiableMap(apiToMongoDocNameMap);
    }

    public static PageRequest getPageRequest(QueryOptions queryOptions) {
        int limit = (int) queryOptions.get("limit");
        int skip = (int) queryOptions.get("skip");
        return getPageRequest(limit, skip);
    }

    public static PageRequest getPageRequest(int limit, String pageToken) {
        int idxCurrentPage = 0;
        if (pageToken != null && !pageToken.isEmpty() && StringUtils.isNumeric(pageToken)) {
            idxCurrentPage = Integer.parseInt(pageToken);
        }
        return getPageRequest(limit, idxCurrentPage * limit);
    }

    public static PageRequest getPageRequest(int limit, int skip) {
        int size = (limit < 0) ? 10 : limit;
        int page = (skip < 0) ? 0 : Math.floorDiv(skip, size);
        return new PageRequest(page, size);
    }

    public static String createExclusionFieldString(List<String> excludeList) {
        List<String> formattedList = excludeList.stream().map(field -> String.format("'%s' : 0", field))
                                                .collect(Collectors.toList());
        return "{ " + String.join(", ", formattedList) + " }";
    }

    private static Map<String, String> initApiToMongoDocNameMap() {
        Map<String, String> map = new HashMap<>();
        map.put("sourceEntries", "files");
        map.put("sourceEntries.statistics", "st");
        map.put("annotation", "annot");
        map.put("sourceEntries.attributes", "files.attrs");
        return map;
    }

    public static <T> QueryResult<T> buildQueryResult(List<T> results) {
        return buildQueryResult(results, results.size());
    }

    public static <T> QueryResult<T> buildQueryResult(List<T> results, long numTotalResults) {
        QueryResult<T> queryResult = new QueryResult<>();
        queryResult.setResult(results);
        queryResult.setNumResults(results.size());
        queryResult.setNumTotalResults(numTotalResults);
        return queryResult;
    }

    public static List<VariantSource> convertVariantSourceEntitysToVariantSources(List<VariantSourceEntity>
                                                                                          variantSourceEntities) {

        List<VariantSource> variantSources = new ArrayList<>();

        for (VariantSourceEntity variantSourceEntity: variantSourceEntities) {
            VariantSource variantSource = new VariantSource(variantSourceEntity.getFileName(),
                                                            variantSourceEntity.getFileId(),
                                                            variantSourceEntity.getStudyId(),
                                                            variantSourceEntity.getStudyName(),
                                                            variantSourceEntity.getType(),
                                                            variantSourceEntity.getAggregation());
            if (variantSourceEntity.getSamplesPosition() != null) {
                variantSource.setSamplesPosition(variantSourceEntity.getSamplesPosition());
            }
            if (variantSourceEntity.getMetadata() != null) {
                variantSource.setMetadata(variantSourceEntity.getMetadata());
            }
            if (variantSourceEntity.getStats() != null) {
                variantSource.setStats(variantSourceEntity.getStats());
            }
            variantSources.add(variantSource);
        }

        return variantSources;
    }

}
