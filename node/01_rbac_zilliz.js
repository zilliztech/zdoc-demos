const fs = require("fs")
const { MilvusClient, DataType, sleep } = require("@zilliz/milvus2-sdk-node")

const address = "YOUR_CLUSTER_ENDPOINT"
const token = "YOUR_CLUSTER_TOKEN"

async function main() {
    // 1. Connect to the cluster
    const client = new MilvusClient({address, token})

    // 2. List roles and users
    var res = await client.listRoles()

    console.log(res.results.map(r => r.role.name))

    // Output
    // 
    // [ 'db_admin', 'db_ro', 'db_rw' ]
    // 

    res = await client.listUsers()

    console.log(res.usernames)

    // Output
    // 
    // [ 'db_admin', 'user1' ]
    // 

    // 3. Create a user

    if (!res.usernames.includes("user1")) {
        await client.createUser({
            username: "user1",
            password: "p@ssw0rd!"
        })
    }

    res = await client.listUsers()

    console.log(res.usernames)

    // Output
    // 
    // [ 'db_admin', 'user1' ]
    // 

    // 4. Update a user credentials

    await client.updateUser({
        username: "user1",
        oldPassword: "p@ssw0rd!",
        newPassword: "p@ssw0rd123!"
    })

    // 5. Describe the role

    var res = await client.describeRole({
        roleName: "db_ro"
    })

    console.log(res.results)

    // Output
    // 
    // [ { users: [], role: { name: 'db_ro' } } ]
    // 

    // 6. Assign a role to a user

    await client.grantRole({
        username: "user1",
        roleName: "db_ro"
    })

    // 7. Describe a user

   res = await client.describeUser({
        username: "user1"
    })

    console.log(res.results)

    // Output
    // 
    // [ { roles: [ [Object] ], user: { name: 'user1' } } ]
    // 

    // 8. Revoke a role from a user

    await client.revokeRole({
        username: "user1",
        roleName: "db_ro"
    })

    // 9. Drop a user

    await client.dropUser({
        username: "user1"
    })
}

main()