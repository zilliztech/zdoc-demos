package com.zilliz.docs;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.DropCollectionReq;
import io.milvus.v2.service.collection.request.GetLoadStateReq;
import io.milvus.v2.service.partition.request.CreatePartitionReq;
import io.milvus.v2.service.vector.request.DeleteReq;
import io.milvus.v2.service.vector.request.InsertReq;
import io.milvus.v2.service.vector.request.UpsertReq;
import io.milvus.v2.service.vector.response.DeleteResp;
import io.milvus.v2.service.vector.response.InsertResp;
import io.milvus.v2.service.vector.response.UpsertResp;

public class InsertUpsertDeleteDemo {
    public static void run() throws InterruptedException {
        String CLUSTER_ENDPOINT = "YOUR_CLUSTER_ENDPOINT";
        String TOKEN = "YOUR_CLUSTER_TOKEN";

        // 1. Connect to Milvus server
        ConnectConfig connectConfig = ConnectConfig.builder()
            .uri(CLUSTER_ENDPOINT)
            .token(TOKEN)
            .build();

        MilvusClientV2 client = new MilvusClientV2(connectConfig);

        // 2. Create a collection in quick setup mode
        CreateCollectionReq quickSetupReq = CreateCollectionReq.builder()
            .collectionName("quick_setup")
            .dimension(5)
            .metricType("IP")
            .build();

        client.createCollection(quickSetupReq);

        GetLoadStateReq loadStateReq = GetLoadStateReq.builder()
            .collectionName("quick_setup")
            .build();

        boolean state = client.getLoadState(loadStateReq);

        System.out.println(state);

        // Output:
        // true




        // 3. Insert some data
        List<JSONObject> data = Arrays.asList(
            new JSONObject(Map.of("id", 0L, "vector", Arrays.asList(0.3580376395471989f, -0.6023495712049978f, 0.18414012509913835f, -0.26286205330961354f, 0.9029438446296592f), "color", "pink_8682")),
            new JSONObject(Map.of("id", 1L, "vector", Arrays.asList(0.19886812562848388f, 0.06023560599112088f, 0.6976963061752597f, 0.2614474506242501f, 0.838729485096104f), "color", "red_7025")),
            new JSONObject(Map.of("id", 2L, "vector", Arrays.asList(0.43742130801983836f, -0.5597502546264526f, 0.6457887650909682f, 0.7894058910881185f, 0.20785793220625592f), "color", "orange_6781")),
            new JSONObject(Map.of("id", 3L, "vector", Arrays.asList(0.3172005263489739f, 0.9719044792798428f, -0.36981146090600725f, -0.4860894583077995f, 0.95791889146345f), "color", "pink_9298")),
            new JSONObject(Map.of("id", 4L, "vector", Arrays.asList(0.4452349528804562f, -0.8757026943054742f, 0.8220779437047674f, 0.46406290649483184f, 0.30337481143159106f), "color", "red_4794")),
            new JSONObject(Map.of("id", 5L, "vector", Arrays.asList(0.985825131989184f, -0.8144651566660419f, 0.6299267002202009f, 0.1206906911183383f, -0.1446277761879955f), "color", "yellow_4222")),
            new JSONObject(Map.of("id", 6L, "vector", Arrays.asList(0.8371977790571115f, -0.015764369584852833f, -0.31062937026679327f, -0.562666951622192f, -0.8984947637863987f), "color", "red_9392")),
            new JSONObject(Map.of("id", 7L, "vector", Arrays.asList(-0.33445148015177995f, -0.2567135004164067f, 0.8987539745369246f, 0.9402995886420709f, 0.5378064918413052f), "color", "grey_8510")),
            new JSONObject(Map.of("id", 8L, "vector", Arrays.asList(0.39524717779832685f, 0.4000257286739164f, -0.5890507376891594f, -0.8650502298996872f, -0.6140360785406336f), "color", "white_9381")),
            new JSONObject(Map.of("id", 9L, "vector", Arrays.asList(0.5718280481994695f, 0.24070317428066512f, -0.3737913482606834f, -0.06726932177492717f, -0.6980531615588608f), "color", "purple_4976"))
        );
   
        InsertReq insertReq = InsertReq.builder()
            .collectionName("quick_setup")
            .data(data)
            .build();

        InsertResp insertResp = client.insert(insertReq);

        System.out.println(JSONObject.toJSON(insertResp));

        // Output:
        // {"insertCnt": 10}




        // 4. Insert some more data into a specific partition
        data = Arrays.asList(
            new JSONObject(Map.of("id", 10L, "vector", Arrays.asList(-0.5570353903748935f, -0.8997887893201304f, -0.7123782431855732f, -0.6298990746450119f, 0.6699215060604258f), "color", "red_1202")),
            new JSONObject(Map.of("id", 11L, "vector", Arrays.asList(0.6319019033373907f, 0.6821488267878275f, 0.8552303045704168f, 0.36929791364943054f, -0.14152860714878068f), "color", "blue_4150")),
            new JSONObject(Map.of("id", 12L, "vector", Arrays.asList(0.9483947484855766f, -0.32294203351925344f, 0.9759290319978025f, 0.8262982148666174f, -0.8351194181285713f), "color", "orange_4590")),
            new JSONObject(Map.of("id", 13L, "vector", Arrays.asList(-0.5449109892498731f, 0.043511240563786524f, -0.25105249484790804f, -0.012030655265886425f, -0.0010987671273892108f), "color", "pink_9619")),
            new JSONObject(Map.of("id", 14L, "vector", Arrays.asList(0.6603339372951424f, -0.10866551787442225f, -0.9435597754324891f, 0.8230244263466688f, -0.7986720938400362f), "color", "orange_4863")),
            new JSONObject(Map.of("id", 15L, "vector", Arrays.asList(-0.8825129181091456f, -0.9204557711667729f, -0.935350065513425f, 0.5484069690287079f, 0.24448151140671204f), "color", "orange_7984")),
            new JSONObject(Map.of("id", 16L, "vector", Arrays.asList(0.6285586391568163f, 0.5389064528263487f, -0.3163366239905099f, 0.22036279378888013f, 0.15077052220816167f), "color", "blue_9010")),
            new JSONObject(Map.of("id", 17L, "vector", Arrays.asList(-0.20151825016059233f, -0.905239387635804f, 0.6749305353372479f, -0.7324272081377843f, -0.33007998971889263f), "color", "blue_4521")),
            new JSONObject(Map.of("id", 18L, "vector", Arrays.asList(0.2432286610792349f, 0.01785636564206139f, -0.651356982731391f, -0.35848148851027895f, -0.7387383128324057f), "color", "orange_2529")),
            new JSONObject(Map.of("id", 19L, "vector", Arrays.asList(0.055512329053363674f, 0.7100266349039421f, 0.4956956543575197f, 0.24541352586717702f, 0.4209030729923515f), "color", "red_9437"))
        );

        CreatePartitionReq createPartitionReq = CreatePartitionReq.builder()
            .collectionName("quick_setup")
            .partitionName("partitionA")
            .build();

        client.createPartition(createPartitionReq);
        
        insertReq = InsertReq.builder()
            .collectionName("quick_setup")
            .data(data)
            .partitionName("partitionA")
            .build();

        insertResp = client.insert(insertReq);

        System.out.println(JSONObject.toJSON(insertResp));

        // Output:
        // {"insertCnt": 10}




        // 5. Upsert some data
        data = Arrays.asList(
            new JSONObject(Map.of("id", 0L, "vector", Arrays.asList(-0.619954382375778f, 0.4479436794798608f, -0.17493894838751745f, -0.4248030059917294f, -0.8648452746018911f), "color", "black_9898")),
            new JSONObject(Map.of("id", 1L, "vector", Arrays.asList(0.4762662251462588f, -0.6942502138717026f, -0.4490002642657902f, -0.628696575798281f, 0.9660395877041965f), "color", "red_7319")),
            new JSONObject(Map.of("id", 2L, "vector", Arrays.asList(-0.8864122635045097f, 0.9260170474445351f, 0.801326976181461f, 0.6383943392381306f, 0.7563037341572827f), "color", "white_6465")),
            new JSONObject(Map.of("id", 3L, "vector", Arrays.asList(0.14594326235891586f, -0.3775407299900644f, -0.3765479013078812f, 0.20612075380355122f, 0.4902678929632145f), "color", "orange_7580")),
            new JSONObject(Map.of("id", 4L, "vector", Arrays.asList(0.4548498669607359f, -0.887610217681605f, 0.5655081329910452f, 0.19220509387904117f, 0.016513983433433577f), "color", "red_3314")),
            new JSONObject(Map.of("id", 5L, "vector", Arrays.asList(0.11755001847051827f, -0.7295149788999611f, 0.2608115847524266f, -0.1719167007897875f, 0.7417611743754855f), "color", "black_9955")),
            new JSONObject(Map.of("id", 6L, "vector", Arrays.asList(0.9363032158314308f, 0.030699901477745373f, 0.8365910312319647f, 0.7823840208444011f, 0.2625222076909237f), "color", "yellow_2461")),
            new JSONObject(Map.of("id", 7L, "vector", Arrays.asList(0.0754823906014721f, -0.6390658668265143f, 0.5610517334334937f, -0.8986261118798251f, 0.9372056764266794f), "color", "white_5015")),
            new JSONObject(Map.of("id", 8L, "vector", Arrays.asList(-0.3038434006935904f, 0.1279149203380523f, 0.503958664270957f, -0.2622661156746988f, 0.7407627307791929f), "color", "purple_6414")),
            new JSONObject(Map.of("id", 9L, "vector", Arrays.asList(-0.7125086947677588f, -0.8050968321012257f, -0.32608864121785786f, 0.3255654958645424f, 0.26227968923834233f), "color", "brown_7231"))
        );

        UpsertReq upsertReq = UpsertReq.builder()
            .collectionName("quick_setup")
            .data(data)
            .build();

        UpsertResp upsertResp = client.upsert(upsertReq);

        System.out.println(JSONObject.toJSON(upsertResp));

        // Output:
        // {"upsertCnt": 10}




        // 6. Upsert data in parition

        data = Arrays.asList(
            new JSONObject(Map.of("id", 10L, "vector", Arrays.asList(0.06998888224297328f, 0.8582816610326578f, -0.9657938677934292f, 0.6527905683627726f, -0.8668460657158576f), "color", "black_3651")),
            new JSONObject(Map.of("id", 11L, "vector", Arrays.asList(0.6060703043917468f, -0.3765080534566074f, -0.7710758854987239f, 0.36993888322346136f, 0.5507513364206531f), "color", "grey_2049")),
            new JSONObject(Map.of("id", 12L, "vector", Arrays.asList(-0.9041813104515337f, -0.9610546012461163f, 0.20033003106083358f, 0.11842506351635174f, 0.8327356724591011f), "color", "blue_6168")),
            new JSONObject(Map.of("id", 13L, "vector", Arrays.asList(0.3202914977909075f, -0.7279137773695252f, -0.04747830871620273f, 0.8266053056909548f, 0.8277957187455489f), "color", "blue_1672")),
            new JSONObject(Map.of("id", 14L, "vector", Arrays.asList(0.2975811497890859f, 0.2946936202691086f, 0.5399463833894609f, 0.8385334966677529f, -0.4450543984655133f), "color", "pink_1601")),
            new JSONObject(Map.of("id", 15L, "vector", Arrays.asList(-0.04697464305600074f, -0.08509022265734134f, 0.9067184632552001f, -0.2281912685064822f, -0.9747503428652762f), "color", "yellow_9925")),
            new JSONObject(Map.of("id", 16L, "vector", Arrays.asList(-0.9363075919673911f, -0.8153981031085669f, 0.7943039120490902f, -0.2093886809842529f, 0.0771191335807897f), "color", "orange_9872")),
            new JSONObject(Map.of("id", 17L, "vector", Arrays.asList(-0.050451522820639916f, 0.18931572752321935f, 0.7522886192190488f, -0.9071793089474034f, 0.6032647330692296f), "color", "red_6450")),
            new JSONObject(Map.of("id", 18L, "vector", Arrays.asList(-0.9181544231141592f, 0.6700755998126806f, -0.014174674636136642f, 0.6325780463623432f, -0.49662222164032976f), "color", "purple_7392")),
            new JSONObject(Map.of("id", 19L, "vector", Arrays.asList(0.11426945899602536f, 0.6089190684002581f, -0.5842735738352236f, 0.057050610092692855f, -0.035163433018196244f), "color", "pink_4996"))
        );

        upsertReq = UpsertReq.builder()
            .collectionName("quick_setup")
            .data(data)
            .partitionName("partitionA")
            .build();

        upsertResp = client.upsert(upsertReq);

        System.out.println(JSONObject.toJSON(upsertResp));

        // Output:
        // {"upsertCnt": 10}




        // 7. Delete entities

        DeleteReq deleteReq = DeleteReq.builder()
            .collectionName("quick_setup")
            .filter("id in [4, 5, 6]")
            .build();

        DeleteResp deleteResp = client.delete(deleteReq);

        System.out.println(JSONObject.toJSON(deleteResp));

        // Output:
        // {"deleteCnt": 3}




        deleteReq = DeleteReq.builder()
            .collectionName("quick_setup")
            .ids(Arrays.asList(18L, 19L))
            .partitionName("partitionA")
            .build();

        deleteResp = client.delete(deleteReq);

        System.out.println(JSONObject.toJSON(deleteResp));

        // Output:
        // {"deleteCnt": 2}




        // 8. Drop collection

        DropCollectionReq dropCollectionReq = DropCollectionReq.builder()
            .collectionName("quick_setup")
            .build();


        client.dropCollection(dropCollectionReq);
    }

    public static void main(String[] args) {
        try {
            run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}