package com.zilliz.docs;

import java.util.List;

import com.alibaba.fastjson.JSONObject;

import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.service.rbac.request.CreateRoleReq;
import io.milvus.v2.service.rbac.request.CreateUserReq;
import io.milvus.v2.service.rbac.request.DescribeRoleReq;
import io.milvus.v2.service.rbac.request.DescribeUserReq;
import io.milvus.v2.service.rbac.request.DropRoleReq;
import io.milvus.v2.service.rbac.request.DropUserReq;
import io.milvus.v2.service.rbac.request.GrantPrivilegeReq;
import io.milvus.v2.service.rbac.request.GrantRoleReq;
import io.milvus.v2.service.rbac.request.RevokePrivilegeReq;
import io.milvus.v2.service.rbac.request.RevokeRoleReq;
import io.milvus.v2.service.rbac.response.DescribeRoleResp;
import io.milvus.v2.service.rbac.response.DescribeUserResp;

public class MilvusRbacDemo {
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
        
        // 2. Create a role
        List<String> roleNames = client.listRoles();

        System.out.println(roleNames);

        // Output:
        // [
        //     "admin",
        //     "public"
        // ]





        if (roleNames.contains("read_only")) {
            DropRoleReq dropRoleReq = DropRoleReq.builder()
                .roleName("read_only")
                .build();

            client.dropRole(dropRoleReq);
        }

        CreateRoleReq createRoleReq = CreateRoleReq.builder()
            .roleName("read_only")
            .build();

        client.createRole(createRoleReq);

        roleNames = client.listRoles();

        System.out.println(roleNames);

        // Output:
        // [
        //     "admin",
        //     "public",
        //     "read_only"
        // ]





        
        // 3. Grant permissions to the role
        GrantPrivilegeReq grantPrivilegeReq = GrantPrivilegeReq.builder()
            .roleName("read_only")
            .objectType("Global")
            .objectName("*")
            .privilege("DescribeCollection")
            .build();

        client.grantPrivilege(grantPrivilegeReq);

        // 4. Describe the role
        DescribeRoleReq describeRoleReq = DescribeRoleReq.builder()
            .roleName("read_only")
            .build();

        DescribeRoleResp describeRoleResp = client.describeRole(describeRoleReq);

        System.out.println(JSONObject.toJSON(describeRoleResp));

        // Output:
        // {"grantInfos": [{
        //     "dbName": "default",
        //     "objectName": "*",
        //     "grantor": "root",
        //     "privilege": "DescribeCollection",
        //     "objectType": "Global"
        // }]}





        // 5. Create a user
        List<String> userNames = client.listUsers();

        System.out.println(userNames);

        // Output:
        // ["root"]





        if (userNames.contains("alice")) {
            DropUserReq dropUserReq = DropUserReq.builder()
                .userName("alice")
                .build();

            client.dropUser(dropUserReq);
        }

        CreateUserReq createUserReq = CreateUserReq.builder()
            .userName("alice")
            .password("123456")
            .build();

        client.createUser(createUserReq);

        userNames = client.listUsers();

        System.out.println(userNames);

        // Output:
        // [
        //     "alice",
        //     "root"
        // ]





        // 6. Assign the role to the user
        GrantRoleReq grantRoleReq = GrantRoleReq.builder()
            .userName("alice")
            .roleName("read_only")
            .build();

        client.grantRole(grantRoleReq);

        // 7. Describe the user
        DescribeUserReq describeUserReq = DescribeUserReq.builder()
            .userName("alice")
            .build();

        DescribeUserResp describeUserResp = client.describeUser(describeUserReq);

        System.out.println(JSONObject.toJSON(describeUserResp));

        // Output:
        // {"roles": ["read_only"]}





        // 8. Revoke the role from the user
        RevokeRoleReq revokeRoleReq = RevokeRoleReq.builder()
            .userName("alice")
            .roleName("read_only")
            .build();

        client.revokeRole(revokeRoleReq);

        // 9. Drop the user
        DropUserReq dropUserReq = DropUserReq.builder()
            .userName("alice")
            .build();

        client.dropUser(dropUserReq);

        userNames = client.listUsers();

        System.out.println(userNames);

        // Output:
        // ["root"]





        // 10. Revoke permissions from the role
        RevokePrivilegeReq revokePrivilegeReq = RevokePrivilegeReq.builder()
            .roleName("read_only")
            .objectType("Global")
            .objectName("*")
            .privilege("DescribeCollection")
            .build();

        client.revokePrivilege(revokePrivilegeReq);

        // 11. Drop the role
        DropRoleReq dropRoleReq = DropRoleReq.builder()
            .roleName("read_only")
            .build();
            
        client.dropRole(dropRoleReq);

        roleNames = client.listRoles();

        System.out.println(roleNames);

        // Output:
        // [
        //     "admin",
        //     "public"
        // ]





    }

    public static void main(String[] args) {
        try {
            run();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}