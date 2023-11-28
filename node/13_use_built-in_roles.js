const { MilvusClient } = require("@zilliz/milvus2-sdk-node")

const address = "YOUR_CLUSTER_ENDPOINT"
const token = "YOUR_CLUSTER_TOKEN"

// Include the following in an async function declaration
async function main () {

    // 1. Connect to the cluster
    const client = new MilvusClient({address, token})

    // 2. Create user
    const users1 = await client.listUsers()

    if (!users1.usernames.includes("user1")) {
        await client.createUser({
            username: "user1",
            password: "P@ssw0rd!",
        })
    }

    // 3. Update user credentials

    await client.updateUser({
        username: "user1",
        oldPassword: "P@ssw0rd!",
        newPassword: "P@ssw0rd!!",
    })

    // 4. List users

    const users = await client.listUsers()

    console.log(users.usernames)

    // Output
    // 
    // [ 'db_admin', 'user1' ]
    // 

    // 5. Assigne role to user

    await client.addUserToRole({
        username: "user1",
        rolename: "db_ro",
    })

    // 6. List roles

    const roles = await client.listRoles()

    console.log(roles.results)

    // Output
    // 
    // [
    //   { users: [ [Object] ], role: { name: 'admin' } },
    //   { users: [ [Object] ], role: { name: 'db_admin' } },
    //   { users: [], role: { name: 'db_ro' } },
    //   { users: [], role: { name: 'db_rw' } },
    //   { users: [], role: { name: 'public' } }
    // ]
    // 

    // 7. Remove user from role

    await client.removeUserFromRole({
        username: "user1",
        rolename: "db_ro",
    })

    // 7. Delete user

    await client.deleteUser({
        username: "user1",
    })

}

main()