const fs = require("fs")
const { MilvusClient, DataType, sleep } = require("@zilliz/milvus2-sdk-node")

const address = "YOUR_CLUSTER_ENDPOINT"
const token = "YOUR_CLUSTER_TOKEN"

async function main() {
    // 1. Connect to the cluster
    const client = new MilvusClient({address, token})

    // 2. Create a role
    var res = await client.listRoles()

    console.log(res.results)

    // Output
    // 
    // [
    //   { users: [], role: { name: 'admin' } },
    //   { users: [], role: { name: 'public' } }
    // ]
    // 

    res =await client.createRole({
        roleName: "read_only"
    })

    console.log(res.error_code)

    // Output
    // 
    // Success
    // 

    res = await client.listRoles()

    console.log(res.results)

    // Output
    // 
    // [
    //   { users: [], role: { name: 'admin' } },
    //   { users: [], role: { name: 'public' } },
    //   { users: [], role: { name: 'read_only' } }
    // ]
    // 

    // 3. Grant permissions
    res = await client.grantPrivilege({
        roleName: "read_only",
        object: "Global",
        objectName: "*",
        privilegeName: "DescribeCollection"
    })

    console.log(res.error_code)

    // Output
    // 
    // Success
    // 

    // 4. Describe the role
    res = await client.describeRole({
        roleName: "read_only"
    })

    console.log(res.results)

    // Output
    // 
    // [ { users: [], role: { name: 'read_only' } } ]
    // 

    // 5. Create a user
    res = await client.listUsers()

    console.log(res.usernames)

    // Output
    // 
    // [ 'root' ]
    // 

    res = await client.createUser({
        username: "alice",
        password: "123456"
    })

    console.log(res.error_code)

    // Output
    // 
    // Success
    // 

    res = await client.listUsers()

    console.log(res.user_names)

    // Output
    // 
    // undefined
    // 

    // 6. Assign a role to a user
    res = await client.grantRole({
        username: "alice",
        roleName: "read_only"
    })

    console.log(res.error_code)

    // Output
    // 
    // Success
    // 

    // 7. Describe the user
    res = await client.describeUser({
        username: "alice"
    })

    console.log(res.results)

    // Output
    // 
    // [ { roles: [ [Object] ], user: { name: 'alice' } } ]
    // 

    // 8. Revoke the role from the user
    res = await client.revokeRole({
        username: "alice",
        roleName: "read_only"
    })

    console.log(res.error_code)

    // Output
    // 
    // Success
    // 

    // 9. Drop the user
    res = await client.dropUser({
        username: "alice"
    })

    console.log(res.error_code)

    // Output
    // 
    // Success
    // 

    // 10. Revoke permissions
    res = await client.revokePrivilege({
        roleName: "read_only",
        object: "Global",
        objectName: "*",
        privilegeName: "DescribeCollection"    
    })

    console.log(res.error_code)

    // Output
    // 
    // Success
    // 

    // 11. Drop the role
    res = await client.dropRole({
        roleName: "read_only"
    })

    console.log(res.error_code)

    // Output
    // 
    // Success
    // 

    res = await client.listRoles()

    console.log(res.results)

    // Output
    // 
    // [
    //   { users: [], role: { name: 'admin' } },
    //   { users: [], role: { name: 'public' } }
    // ]
    // 

}

main()