package com.zilliz.docs;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ProtocolStringList;

import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.ListCredUsersResponse;
import io.milvus.grpc.RoleResult;
import io.milvus.grpc.SelectRoleResponse;
import io.milvus.grpc.SelectUserResponse;
import io.milvus.grpc.UserResult;
import io.milvus.param.ConnectParam;
import io.milvus.param.R;
import io.milvus.param.RpcStatus;
import io.milvus.param.credential.CreateCredentialParam;
import io.milvus.param.credential.DeleteCredentialParam;
import io.milvus.param.credential.ListCredUsersParam;
import io.milvus.param.credential.UpdateCredentialParam;
import io.milvus.param.role.AddUserToRoleParam;
import io.milvus.param.role.SelectRoleParam;
import io.milvus.param.role.SelectUserParam;

/**
 * Hello world!
 */
public final class UseBuiltInRolesDemo {
    private UseBuiltInRolesDemo() {
    }

    /**
     * Says hello to the world.
     * @param args The arguments of the program.
     */
    public static void main(String[] args) {
        String clusterEndpoint = "YOUR_CLUSTER_ENDPOINT";
        String token = "YOUR_CLUSTER_TOKEN";

        // 1. Connect to Zilliz Cloud cluster
        ConnectParam connectParam = ConnectParam.newBuilder()
            .withUri(clusterEndpoint)
            .withToken(token)
            .build();

        MilvusServiceClient client = new MilvusServiceClient(connectParam);

        System.out.println("Connected to Zilliz Cloud!");

        // Output:
        // Connected to Zilliz Cloud!





        
        // 2. Create a user

        CreateCredentialParam createCredentialParam = CreateCredentialParam.newBuilder()
            .withUsername("user1")
            .withPassword("P@ssw0rd!")
            .build();

        R<RpcStatus> res = client.createCredential(createCredentialParam);

        if (res.getException() != null) {
            System.err.println("Failed to create user!");
            return;
        }

        System.out.println("User created!");

        // Output:
        // User created!




        // 3. Update user credential

        UpdateCredentialParam updateCredentialParam = UpdateCredentialParam.newBuilder()
            .withUsername("user1")
            .withOldPassword("P@ssw0rd!")
            .withNewPassword("P@ssw0rd!!")
            .build();

        R<RpcStatus> updateCreRes = client.updateCredential(updateCredentialParam);

        if (updateCreRes.getException() != null) {
            System.err.println("Failed to update user credential!");
            return;
        }

        System.out.println("User credential updated!");

        // Output:
        // User credential updated!




        // 4. List users

        ListCredUsersParam listCredUsersParam = ListCredUsersParam.newBuilder()
            .build();

        R<ListCredUsersResponse> listRes = client.listCredUsers(listCredUsersParam);

        if (listRes.getException() != null) {
            System.err.println("Failed to list users!");
            return;
        }

        ProtocolStringList usernames = listRes.getData().getUsernamesList();

        System.out.println(usernames);

        // Output:
        // [
        //     "db_admin",
        //     "user1"
        // ]




        // 5. Assigne roles to user

        AddUserToRoleParam addUserToRoleParam = AddUserToRoleParam.newBuilder()
            .withUserName("user1")
            .withRoleName("db_ro")
            .build();

        R<RpcStatus> addRes = client.addUserToRole(addUserToRoleParam);

        if (addRes.getException() != null) {
            System.err.println("Failed to assign role to user!");
            return;
        }

        System.out.println("Role assigned to user!");

        // Output:
        // Role assigned to user!




        // 6. Get users of a specific role

        SelectRoleParam selectRoleParam = SelectRoleParam.newBuilder()
            .withRoleName("db_ro")
            .withIncludeUserInfo(true)
            .build();

        R<SelectRoleResponse> selectRoleRes = client.selectRole(selectRoleParam);

        if (selectRoleRes.getException() != null) {
            System.err.println("Failed to list roles!");
            return;
        }

        List<RoleResult> roles = selectRoleRes.getData().getResultsList();
        List<JSONObject> roleList = new ArrayList<>();

        for (RoleResult role : roles) {
            role.getAllFields().forEach((k, v) -> {
                roleList.add(new JSONObject().fluentPut(k.getName(), v));
            });
        }

        System.out.println(roleList);

        // Output:
        // [
        //     {"role": {"name": "db_ro"}},
        //     {"users": [{"name": "user1"}]}
        // ]



            
        // 7. Get roles of a specific user

        SelectUserParam selectUserParam = SelectUserParam.newBuilder()
            .withUserName("user1")
            .withIncludeRoleInfo(true)
            .build();

        R<SelectUserResponse> selectUserRes = client.selectUser(selectUserParam);

        if (selectUserRes.getException() != null) {
            System.err.println("Failed to list roles!");
            return;
        }

        List<UserResult> users = selectUserRes.getData().getResultsList();
        List<JSONObject> userList = new ArrayList<>();

        for (UserResult user : users) {
            user.getAllFields().forEach((k, v) -> {
                userList.add(new JSONObject().fluentPut(k.getName(), v));
            });
        }

        System.out.println(userList);

        // Output:
        // [
        //     {"user": {"name": "user1"}},
        //     {"roles": [{"name": "db_ro"}]}
        // ]




        // 8. Delete user

        DeleteCredentialParam  deleteCredentialParam = DeleteCredentialParam.newBuilder()
            .withUsername("user1")
            .build();

        R<RpcStatus> deleteRes = client.deleteCredential(deleteCredentialParam);

        if (deleteRes.getException() != null) {
            System.err.println("Failed to delete user!");
            return;
        }

        System.out.println("User deleted!");

        // Output:
        // User deleted!



    }
}