package com.zilliz.docs;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.rbac.request.CreateUserReq;
import io.milvus.v2.service.rbac.request.DescribeUserReq;
import io.milvus.v2.service.rbac.request.DropUserReq;
import io.milvus.v2.service.rbac.request.GrantRoleReq;
import io.milvus.v2.service.rbac.request.RevokeRoleReq;
import io.milvus.v2.service.rbac.response.DescribeUserResp;

public class ZillizRbacDemo {

    public static void run() throws InterruptedException {
        String CLUSTER_ENDPOINT = "YOUR_CLUSTER_ENDPOINT";
        String TOKEN = "YOUR_CLUSTER_TOKEN";

        // 1. Connect to Milvus server
        ConnectConfig connectConfig = ConnectConfig.builder()
            .uri(CLUSTER_ENDPOINT)
            .token(TOKEN)
            .secure(false)
            .build();

        MilvusClientV2 client = new MilvusClientV2(connectConfig); 

        // 2. List all roles
        List<String> roleNames = client.listRoles();

        System.out.println(roleNames);

        // 3. List all users
        List<String> userNames = client.listUsers();

        System.out.println(userNames);

        // 4. Create a user
        CreateUserReq createUserReq = CreateUserReq.builder()
           .userName("alice")
           .password("123456")
           .build();
        
        client.createUser(createUserReq);

        userNames = client.listUsers();

        System.out.println(userNames);

        // 5. Assign a role to a user
        GrantRoleReq grantRoleReq = GrantRoleReq.builder()
           .userName("alice")
           .roleName("db_ro")
           .build();

        client.grantRole(grantRoleReq);

        // 6. Describe the user
        DescribeUserReq describeUserReq = DescribeUserReq.builder()
           .userName("alice")
           .build();

        DescribeUserResp describeUserResp = client.describeUser(describeUserReq);

        System.out.println(JSONObject.toJSON(describeUserResp));

        // 7. Revoke a role from a user
        RevokeRoleReq revokeRoleReq = RevokeRoleReq.builder()
           .userName("alice")
           .roleName("db_ro")
           .build();

        client.revokeRole(revokeRoleReq);

        // 8. Drop the user
        DropUserReq dropUserReq = DropUserReq.builder()
           .userName("alice")
           .build();

        client.dropUser(dropUserReq);

        userNames = client.listUsers();

        System.out.println(userNames);
    }

    public static void main(String[] args) {
        try {
            run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
