var selectedItemId;
var selectedCustomerId;
var fullTotal=0;

generateOrderId();
setDate();
loadAllCustomerIds();
loadAllItemIds();

$("#btnAddCad").click(function () {
    //console.log("BatMAN");
    //alert("FUQ");
    addCart();
    clearInputItems();

});

$("#purchaseBtn").click(function () {
    purchaseOrder();
});

/*===============load customer and item ids ===============*/


$("#idCmbOrder").change(function (e) {
     selectedCustomerId = $('#idCmbOrder').find(":selected").text();
    selectedCustomer(selectedCustomerId);
});


$("#idCmbItem").change(function (e) {
    selectedItemId = $('#idCmbItem').find(":selected").text();
    selectedItem(selectedItemId);
});

$("#dis").keyup(function (event) {
    discountCal();
});

$("#cash").keyup(function (event) {
    let subTotal = parseInt($("#subTotal").text());
    let cash = parseInt($("#cash").val());
    let balance = cash - subTotal;
    $("#balanceCash").val(balance);
});

/* load customer ids to cmb (customer)*/
function loadAllCustomerIds() {
    $("#idCmbOrder").empty();
    let cusHint = `<option disabled selected>Select Customer ID</option>`;
    $("#idCmbOrder").append(cusHint);
    $.ajax({
        url: "http://localhost:8080/backEnd/order?option=cmb_load_cus_id",
        method: "GET",
        success: function (resp) {
            if (resp.status == 200) {
                for (const customer of resp.data) {
                    let option = `<option value="${customer.id}">${customer.id}</option>`;
                    $("#idCmbOrder").append(option);
                }
            } else {
                alert(resp.data);
            }
        }
    })

    /* let cusHint=`<option disabled selected> Select Customer ID</option>`;

     $("#idCmbOrder").append(cusHint);


     for (let i in customerDB) {
         let option = `<option value="${customerDB[i].getCustomerID()}">${customerDB[i].getCustomerID()}</option>`
         $("#idCmbOrder").append(option);
     }*/
}

/*load customer data to text fields*/
function selectedCustomer(CustomerId) {
    $.ajax({
        url: `http://localhost:8080/backEnd/order?option=SelectedCus&cusId=${CustomerId}`,
        method: "GET",
        success: function (resp) {
            if (resp.status == 200) {
                for (const customer of resp.data) {
                    $("#cusName").val(customer.cusName);
                    $("#address").val(customer.cusAddress);
                    $("#tepNumber").val(customer.cusSalary);
                }
            } else {
                alert(resp.data)
            }
        }
    })
    /*for (const i in customerDB){
        if (customerDB[i].getCustomerID()==CustomerId) {
            let element = customerDB[i];
            $("#cusName").val(element.getCustomerName());
            $("#address").val(element.getCustomerAddress());
            $("#tepNumber").val(element.getCustomerTelNumber());
        }
    }*/
}

/* load item ids to cmb (item)*/
function loadAllItemIds() {
    $("#idCmbItem").empty();
    let itemHint = `<option disabled selected>Select Item ID</option>`;
    $("#idCmbItem").append(itemHint);
    $.ajax({
        url: "http://localhost:8080/backEnd/order?option=cmb_load_item_id",
        method: "GET",
        success: function (resp) {
            if (resp.status == 200) {
                for (const item of resp.data) {
                    let option = `<option value="${item.id}">${item.id}</option>`;
                    $("#idCmbItem").append(option);
                }
            } else {
                alert(resp.data);
            }
        }
    })


    /*
        let itemHint=`<option disabled selected> Select Item ID</option>`;

        $("#idCmbItem").append(itemHint);

        for (let i in itemDB) {
            let option = `<option value="${itemDB[i].getItemID()}">${itemDB[i].getItemID()}</option>`
            $("#idCmbItem").append(option);
        }*/
}

/*load item data to text fields*/
function selectedItem(ItemId) {

    $.ajax({
        url: `http://localhost:8080/backEnd/order?option=SelectedItem&itemId=${ItemId}`,
        method: "GET",
        success: function (resp) {
            if (resp.status == 200) {
                for (const item of resp.data) {
                    $("#itemName").val(item.itemName);
                    $("#qtyOnHand").val(item.itemQty);
                    $("#price").val(item.itemPrice);
                }
            } else {
                alert(resp.data)
            }
        }

        /*for (const i in itemDB){
            if (itemDB[i].getItemID()==ItemId) {
                let element = itemDB[i];
                $("#itemName").val(element.getItemName());
                $("#qtyOnHand").val(element.getItemQty());
                $("#price").val(element.getItemPrice());
            }
        }*/
    });
}

////////////////////////////////////////////////////////////////////////////////////////

//generate order Id

function generateOrderId() {
    $.ajax({
        url: "http://localhost:8080/backEnd/order?option=GenOrderId", method: 'GET', success: function (resp) {
            if (resp.status == 200) {
                $("#oId").val(resp.data.oid);
            } else {
                alert(resp.data);
            }
        }
    });
    /*let index = orderDB.length - 1;
    let id;
    let temp;
    if (index != -1) {
        id = orderDB[orderDB.length - 1].getOrderId();
        temp = id.split("-")[1];
        temp++;
    }

    if (index == -1) {
        $("#oId").val("O00-001");
    } else if (temp <= 9) {
        $("#oId").val("O00-00" + temp);
    } else if (temp <= 99) {
        $("#oId").val("O00-0" + temp);
    } else {
        $("#oId").val("O00-" + temp);
    }*/



}

//set date

function setDate() {
    let d = new Date();
    let dd = d.toISOString().split("T")[0].split("-");
    $("#iDate").val(dd[0] + "-" + dd[1] + "-" + dd[2]);
    $("#iDate").text(dd[0] + "-" + dd[1] + "-" + dd[2]);
}

function addCart() {

    let itemCode = selectedItemId;
    console.log(itemCode);
    let itemName = $("#itemName").val();
    let qtyOnHand = parseInt($("#qtyOnHand").val());
    let price = $("#price").val();
    let orderQty = parseInt($("#oQty").val());
    let total = 0;

    if (qtyOnHand + 1 <= orderQty) {

        alert("Enter Valid QTY");
        $("#oQty").val("");
        return;
    }
    qtyOnHand = qtyOnHand - orderQty;


    //updateing qty

    for (let i = 0; i < itemDB.length; i++) {
        if (itemCode == itemDB[i].getItemId()) {
            itemDB[i].setItemQty(qtyOnHand);
        }
    }

    let newQty = 0;
    let newTotal = 0;

    if (checkDuplicates(itemCode) == -1) {
        total = orderQty * price;
        fullTotal = fullTotal + total;
        let row =
            `<tr><td>${itemCode}</td><td>${itemName}</td><td>${price}</td><td>${qtyOnHand}<td>${total}</td></tr>`;
        $("#orderTbody").append(row);
        $("#lblFullTotal").text(fullTotal + " LKR");


        clearInputItems();

    } else {

        let rowNo = checkDuplicates(itemCode);
        newQty = orderQty;
        let oldQty = parseInt($($('#orderTbody>tr').eq(rowNo).children(":eq(3)")).text());
        let oldTotal = parseInt($($('#orderTbody>tr').eq(rowNo).children(":eq(4)")).text());

        fullTotal = fullTotal - oldTotal;
        newQty = parseInt(oldQty) + parseInt(newQty);
        newTotal = newQty * price;
        fullTotal = fullTotal + newTotal;

        //Update row
        $('#orderTbody tr').eq(rowNo).children(":eq(3)").text(newQty);
        $('#orderTbody tr').eq(rowNo).children(":eq(4)").text(newTotal);

        $("#lblFullTotal").text(fullTotal + " LKR");
        $("#subTotal").text(fullTotal + " LKR");
        clearInputItems();
    }


}

function checkDuplicates(itemId) {
    for (let i = 0; i < $("#orderTbody> tr").length; i++) {
        if (itemId == $('#orderTbody').children().eq(i).children().eq(0).text()) {
            alert(i);
            return i;
        }

    }
    return -1;
}

function clearInputItems() {
    $("#idCmbItem").val("");
    $("#itemName").val("");
    $("#qtyOnHand").val("");
    $("#price").val("");
    $("#oQty").val("");
}

function discountCal() {

    var discount = 0;
    var discounted_price = 0;
    var tempDiscount = 0;

    discount = parseInt($("#dis").val());
    tempDiscount = 100 - discount;
    discounted_price = (tempDiscount * fullTotal) / 100;
    console.log(typeof discounted_price);
    $("#subTotal").text(discounted_price + " LKR");

}

function purchaseOrder() {
    console.log(selectedCustomerId);
    var obj = {
        order: {
            orderId: $("#oId").val(),
            customer: selectedCustomerId,
            orderDate: $("#iDate").val(),
            discount: parseInt($("#dis").val()),
            total: $("#lblFullTotal").text().split(" ")[0],
            subTotal: $("#subTotal").text().split(" ")[0]
        },
        orderDatail: []
    }
    for (let i = 0; i < $('#orderTbody tr').length; i++) {
        tblItemId = $('#orderTbody').children().eq(i).children().eq(0).text();
        tblItemName = $('#orderTbody').children().eq(i).children().eq(1).text();
        tblItemPrice = $('#orderTbody').children().eq(i).children().eq(2).text();
        tblItemQty = $('#orderTbody').children().eq(i).children().eq(3).text();
        tblItemTotal = $('#orderTbody').children().eq(i).children().eq(4).text();
        var details = {
            itemCode: tblItemId,
            itemName: tblItemName,
            itemPrice: tblItemPrice,
            itemQty: tblItemQty,
            itemTotal: tblItemTotal
        }
        obj.orderDatail.push(details);

    }
    console.log(JSON.stringify(obj));
    $.ajax({
        url: "http://localhost:8080/backEnd/order",
        method: "POST",
        data: JSON.stringify(obj),
        success: function (resp) {
            if (resp.status == 200) {
                generateOrderId();
                clearInputItems();
            } else {
                alert(resp.data);
            }
        }
    });
}