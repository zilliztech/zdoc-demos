package com.zilliz.docs;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.rbac.request.CreateUserReq;
import io.milvus.v2.service.rbac.request.DescribeRoleReq;
import io.milvus.v2.service.rbac.request.DescribeUserReq;
import io.milvus.v2.service.rbac.request.DropUserReq;
import io.milvus.v2.service.rbac.request.GrantRoleReq;
import io.milvus.v2.service.rbac.request.RevokeRoleReq;
import io.milvus.v2.service.rbac.request.UpdatePasswordReq;
import io.milvus.v2.service.rbac.response.DescribeRoleResp;
import io.milvus.v2.service.rbac.response.DescribeUserResp;

public class ZillizRbacDemo {

    public static void run() throws InterruptedException {
        String CLUSTER_ENDPOINT = "YOUR_CLUSTER_ENDPOINT";
        String TOKEN = "YOUR_CLUSTER_TOKEN";

        // 1. Connect to Milvus server
        ConnectConfig connectConfig = ConnectConfig.builder()
            .uri(CLUSTER_ENDPOINT)
            .token(TOKEN)
            .secure(true)
            .build();

        MilvusClientV2 client = new MilvusClientV2(connectConfig); 

        // 2. List all users and roles
        List<String> roleNames = client.listRoles();

        System.out.println(roleNames);

        // Output:
        // [
        //     "db_admin",
        //     "db_ro",
        //     "db_rw"
        // ]




        List<String> userNames = client.listUsers();

        System.out.println(userNames);

        // Output:
        // ["db_admin"]




        // 3. Create a user
        CreateUserReq createUserReq = CreateUserReq.builder()
           .userName("user1")
           .password("p@ssw0rd!")
           .build();
        
        client.createUser(createUserReq);

        userNames = client.listUsers();

        System.out.println(userNames);

        // Output:
        // [
        //     "db_admin",
        //     "user1"
        // ]




        // 4. Update user password
        UpdatePasswordReq updatePasswordReq = UpdatePasswordReq.builder()
           .userName("user1")
           .password("p@ssw0rd!")
           .newPassword("p@ssw0rd123!")
           .build();

        client.updatePassword(updatePasswordReq);

        // 5. Describe the role
        DescribeRoleReq describeRoleReq = DescribeRoleReq.builder()
           .roleName("db_ro")
           .build();

        DescribeRoleResp describeRoleResp = client.describeRole(describeRoleReq);

        System.out.println(JSONObject.toJSON(describeRoleResp));

        // Output:
        // {"grantInfos": [
        //     {
        //         "dbName": "default",
        //         "objectName": "*",
        //         "grantor": "",
        //         "privilege": "GetLoadState",
        //         "objectType": "Collection"
        //     },
        //     {
        //         "dbName": "default",
        //         "objectName": "*",
        //         "grantor": "",
        //         "privilege": "GetLoadingProgress",
        //         "objectType": "Collection"
        //     },
        //     {
        //         "dbName": "default",
        //         "objectName": "*",
        //         "grantor": "",
        //         "privilege": "HasPartition",
        //         "objectType": "Collection"
        //     },
        //     {
        //         "dbName": "default",
        //         "objectName": "*",
        //         "grantor": "",
        //         "privilege": "IndexDetail",
        //         "objectType": "Collection"
        //     },
        //     {
        //         "dbName": "default",
        //         "objectName": "*",
        //         "grantor": "",
        //         "privilege": "Load",
        //         "objectType": "Collection"
        //     },
        //     {
        //         "dbName": "default",
        //         "objectName": "*",
        //         "grantor": "",
        //         "privilege": "Query",
        //         "objectType": "Collection"
        //     },
        //     {
        //         "dbName": "default",
        //         "objectName": "*",
        //         "grantor": "",
        //         "privilege": "Search",
        //         "objectType": "Collection"
        //     },
        //     {
        //         "dbName": "default",
        //         "objectName": "*",
        //         "grantor": "",
        //         "privilege": "ShowPartitions",
        //         "objectType": "Collection"
        //     },
        //     {
        //         "dbName": "default",
        //         "objectName": "*",
        //         "grantor": "",
        //         "privilege": "DescribeAlias",
        //         "objectType": "Global"
        //     },
        //     {
        //         "dbName": "default",
        //         "objectName": "*",
        //         "grantor": "",
        //         "privilege": "DescribeCollection",
        //         "objectType": "Global"
        //     },
        //     "(3 elements are hidden)"
        // ]}




        // 6. Assign a role to a user
        GrantRoleReq grantRoleReq = GrantRoleReq.builder()
           .userName("user1")
           .roleName("db_ro")
           .build();

        client.grantRole(grantRoleReq);

        // 7. Describe the user
        DescribeUserReq describeUserReq = DescribeUserReq.builder()
           .userName("user1")
           .build();

        DescribeUserResp describeUserResp = client.describeUser(describeUserReq);

        System.out.println(JSONObject.toJSON(describeUserResp));

        // Output:
        // {"roles": ["db_ro"]}




        // 8. Revoke a role from a user
        RevokeRoleReq revokeRoleReq = RevokeRoleReq.builder()
           .userName("user1")
           .roleName("db_ro")
           .build();

        client.revokeRole(revokeRoleReq);

        // 9. Drop the user
        DropUserReq dropUserReq = DropUserReq.builder()
           .userName("user1")
           .build();

        client.dropUser(dropUserReq);

        userNames = client.listUsers();

        System.out.println(userNames);

        // Output:
        // ["db_admin"]



    }

    public static void main(String[] args) {
        try {
            run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}