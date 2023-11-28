package main

import (
	"context"
	"fmt"
	"log"

	"github.com/milvus-io/milvus-sdk-go/v2/client"
)

func main() {
	CLUSTER_ENDPOINT := "YOUR_CLUSTER_ENDPOINT"
	TOKEN := "YOUR_CLUSTER_TOKEN"
	USERNAME := "user1"
	PASSWORD1 := "P@ssw0rd!"
	PASSWORD2 := "P@ssw0rd!!"

	// 1. Connect to cluster

	connParams := client.Config{
		Address: CLUSTER_ENDPOINT,
		APIKey:  TOKEN,
	}

	conn, err := client.NewClient(context.Background(), connParams)

	if err != nil {
		log.Fatal("Failed to connect to Zilliz Cloud:", err.Error())
	}

	// 2. Create a user

	err = conn.CreateCredential(
		context.Background(), // context
		USERNAME,             // username
		PASSWORD1,            // password
	)

	if err != nil {
		log.Fatal("Failed to create user:", err.Error())
	}

	// 2. Update user credential

	err = conn.UpdateCredential(
		context.Background(), // context
		USERNAME,             // username
		PASSWORD1,            // old password
		PASSWORD2,            // new password
	)

	if err != nil {
		log.Fatal("Failed to update user credential:", err.Error())
	}

	// 3. List users

	users1, err := conn.ListCredUsers(context.Background())

	if err != nil {
		log.Fatal("Failed to list users:", err.Error())
	}

	fmt.Println("Users:", users1)

	// Output:
	//
	// Users: [db_admin user1]

	// Alternatively

	users2, err := conn.ListUsers(context.Background())

	if err != nil {
		log.Fatal("Failed to list users:", err.Error())
	}

	userList := make([]string, 0)

	for _, user := range users2 {
		userList = append(userList, user.Name)
	}

	fmt.Println("Users:", userList)

	// Output:
	//
	// Users: [db_admin user1]

	// 4. List Roles

	roles, err := conn.ListRoles(
		context.Background(), // context
	)

	if err != nil {
		log.Fatal("Failed to list roles:", err.Error())
	}

	roleList := make([]interface{}, 0, 1)
	for _, role := range roles {
		roleList = append(roleList, role.Name)
	}

	fmt.Println("Roles:", roleList)

	// Output:
	//
	// Roles: [admin db_admin db_ro db_rw public]

	// 5. Assign role

	rolename := "db_ro"

	err = conn.AddUserRole(
		context.Background(), // context
		USERNAME,             // username
		rolename,             // role
	)

	if err != nil {
		log.Fatal("Failed to assign role:", err.Error())
	}

	// 6. Remove user from role

	err = conn.RemoveUserRole(
		context.Background(), // context
		USERNAME,             // username
		rolename,             // role
	)

	if err != nil {
		log.Fatal("Failed to remove user from role:", err.Error())
	}

	// 7. Delete user

	err = conn.DeleteCredential(
		context.Background(), // context
		USERNAME,             // username
	)

	if err != nil {
		log.Fatal("Failed to delete user:", err.Error())
	}

}
