package com.solara.mnote.service;


import com.mnote.quest.grpc.EmbeddingRequest;
import com.mnote.quest.grpc.EmbeddingResponse;
import com.mnote.quest.grpc.QuestServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NoteQueryService extends QuestServiceGrpc.QuestServiceImplBase {

    @Override
    public void getQueryEmbedding(EmbeddingRequest request, StreamObserver<EmbeddingResponse> responseObserver) {
        String rawText = request.getText();
        System.out.println(">>> [Quest Server] Received text extraction request for: " + rawText);

        // TODO: Next step will replace this with the real LM Studio / Spring AI Call
        // Mocking a 768 float array vector for verification
        List<Float> mockVector = new ArrayList<>(768);
        for (int i = 0; i < 768; i++) {
            mockVector.add((float) Math.random());
        }

        EmbeddingResponse response = EmbeddingResponse.newBuilder()
                .addAllEmbedding(mockVector)
                .build();

        // Push data back to the client channel
        responseObserver.onNext(response);
        // Fire completion handshake
        responseObserver.onCompleted();
    }
}