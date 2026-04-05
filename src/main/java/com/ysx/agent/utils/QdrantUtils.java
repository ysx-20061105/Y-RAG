package com.ysx.agent.utils;

import com.google.common.util.concurrent.ListenableFuture;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Points;

import java.util.concurrent.ExecutionException;

public class QdrantUtils {
    /**
     * 创建集合
     *
     * @param client Qdrant连接
     * @param name   集合名称
     * @return
     */
    public static void getCollectionName(QdrantClient client, String name, long dimension) throws ExecutionException, InterruptedException {
        ListenableFuture<Boolean> booleanListenableFuture = client.collectionExistsAsync(name);
        if (!booleanListenableFuture.get()) {
            client.createCollectionAsync(name, Collections.VectorParams.newBuilder()
                    .setDistance(Collections.Distance.Cosine)
                    .setSize(dimension)
                    .build()).get();
        }
    }

    /**
     * 检查collectionName是否存在
     *
     * @param client
     * @param collectionName
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static boolean isCollectionInQdrant(QdrantClient client, String collectionName) throws ExecutionException, InterruptedException {
        ListenableFuture<Boolean> booleanListenableFuture = client.collectionExistsAsync(collectionName);
        return booleanListenableFuture.get();
    }

    /**
     * 根据noteId删除
     *
     * @param client
     * @param collectionName
     * @param noteId
     * @throws Exception
     */
    public static void deleteByNoteId(QdrantClient client, String collectionName, Long noteId) throws Exception {
        Points.Filter filter = Points.Filter.newBuilder()
                .addMust(
                        Points.Condition.newBuilder()
                                .setField(
                                        Points.FieldCondition.newBuilder()
                                                .setKey("note_id")
                                                .setMatch(
                                                        Points.Match.newBuilder()
                                                                .setInteger(noteId) // 对应 Long 类型
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();
        client.deleteAsync(collectionName, filter).get();
    }
}
