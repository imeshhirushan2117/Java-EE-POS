/*===============Item party===============*/
generateId();
addItemData();
/*addItem*/
$("#btnItemAdd").click(function () {
    $.ajax({
        url: "http://localhost:8080/backEnd/item",
        method: "POST",
        data: $("#itemForm").serialize(),
        success: function (resp) {
            if (resp.status == 200) {
                clearFileldItem();
                addItemData();
                generateId();
                loadAllItemIds();
            } else {
                alert(resp.data)
            }
        }, error: function (ob, textStatus, error) {
            console.log(ob);
            console.log(textStatus);
            console.log(error);
        }
    });

    /*let itemId = $("#txtItemID").val();
    let itemName = $("#txtItemName").val();
    let itemQty = $("#txtItemQty").val();
    let itemPrice = $("#txtItemPrice").val();


    var itemOB = new ItemDTO(itemId,itemName,itemQty,itemPrice);

    itemDB.push(itemOB);
    clearFileldItem();
    addItemData();
    generateId();
    loadAllItemIds();
});
function bindItemRow(){
    $("#tbodyItem>tr").click(function () {
        let itemId = $(this).children(":eq(0)").text();
        let itemName = $(this).children(":eq(1)").text();
        let itemQty = $(this).children(":eq(2)").text();
        let itemPrice = $(this).children(":eq(3)").text();

        $("#txtItemID").val(itemId);
        $("#txtItemName").val(itemName);
        $("#txtItemQty").val(itemQty);
        $("#txtItemPrice").val(itemPrice);
    });*/
});

/*table load*/
function addItemData() {

    $("#tbodyItem").empty();
    $.ajax({
        url: "http://localhost:8080/backEnd/item?option=GetAll",
        method: "GET",
        success:function (rest){
            for (const item of rest.data){
                let raw = `<tr><td>${item.itemId}</td><td>${item.itemName}</td><td>${item.itemQty}</td><td>${item.itemPrice}</td></tr>`
                $("#tbodyItem").append(raw);
                itemDelete();
            }
        }
    })

    /*$("#tbodyItem").empty();
    for (var i of itemDB) {
        let raw = `<tr><td>${i.getItemID()}</td><td>${i.getItemName()}</td><td>${i.getItemQty()}</td><td>${i.getItemPrice()}</td></tr>`
        $("#tblItem").append(raw);
        bindItemRow();
        itemDelete();
    }*/
}

/*btnClear*/
$("#btnItemClear").click(function () {
    clearFileldItem();
});

function clearFileldItem() {
    $("#txtItemID,#txtItemName,#txtItemQty,#txtItemPrice").val("");
}

/*textFeeldsForcasing*/
$("#txtItemID").keydown(function (event) {
    if (event.key == "Enter") {
        $("#txtItemName").focus();
    }
});
$("#txtItemName").keydown(function (event) {
    if (event.key == "Enter") {
        $("#txtItemQty").focus();
    }
});
$("#txtItemQty").keydown(function (event) {
    if (event.key == "Enter") {
        $("#txtItemPrice").focus();
    }
});

/*Item Search*/
$("#btnItemSearch").click(function () {
    var searchID = $("#txtItemSearch").val();
    var response = searchItem(searchID);
    if (response) {
        $("#txtItemID").val(response.getItemID());
        $("#txtItemName").val(response.getItemName());
        $("#txtItemQty").val(response.getItemQty());
        $("#txtItemPrice").val(response.getItemPrice());
    } else {
        alert("Invalid Item Search");
        clearFileld();
    }
});

function searchItem(id) {
    for (let i = 0; i < itemDB.length; i++) {
        if (itemDB[i].getItemID() == id) {
            return itemDB[i];
        }
    }
}

/*item Delete*/
function itemDelete() {
    $("#btnItemDelete").click(function () {
        let itemId = $("#txtItemID").val();
        for (let i = 0; i < itemDB.length; i++) {
            if (itemDB[i].getItemID() == itemId) {
                itemDB.splice(i, 1);
            }
        }
        addItemData()
        clearFileldItem();
        generateId();
    });
}


/*Item Update*/
$("#btnItemUpdate").click(function () {
    let itemId = $("#txtItemID").val();
    let itemName = $("#txtItemName").val();
    let itemQty = $("#txtItemQty").val();
    let itemPrice = $("#txtItemPrice").val();

    for (var i = 0; i < itemDB.length; i++) {
        if (itemDB[i].getItemID() == itemId) {
            itemDB[i].setItemName(itemName);
            itemDB[i].setItemQty(itemQty);
            itemDB[i].setItemPrice(itemPrice);
        }
    }
    addItemData();
    generateId();
    clearFileldItem();
});

/*Item ID auto generate*/
function generateId() {
    $.ajax({
        url: "http://localhost:8080/backEnd/item?option=GenId",
        method: "GET",
        success:function (resp){
            if(resp.status==200){
                $("#txtItemID").val(resp.data.code);
            }else{
                alert(resp.data);
            }
        }
    });

    /*let index = itemDB.length - 1;
    let id;
    let temp;
    if (index != -1) {
        id = itemDB[itemDB.length - 1].getItemID();
        temp = id.split("-")[1];
        temp++;
    }

    if (index == -1) {
        $("#txtItemID").val("I00-001");
    } else if (temp <= 9) {
        $("#txtItemID").val("I00-00" + temp);
    } else if (temp <= 99) {
        $("#txtItemID").val("I00-0" + temp);
    } else {
        $("#txtItemID").val("I00-" + temp);
    }*/
}
