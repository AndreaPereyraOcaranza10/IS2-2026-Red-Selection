const form = document.getElementById("myForm");
const imgPreview = document.getElementById("imgPreview");
const fileInput = document.getElementById("imgInput");
const nameInput = document.getElementById("name");
const ageInput = document.getElementById("age");
const cityInput = document.getElementById("city");
const emailInput = document.getElementById("email");
const phoneInput = document.getElementById("phone");
const jobInput = document.getElementById("job");
const sDateInput = document.getElementById("sDate");

const userInfo = document.getElementById("data");
const userModalEl = document.getElementById("userForm");
const modalTitle = document.querySelector("#userForm .modal-title");
const submitBtn = document.querySelector("#userForm button[type='submit']");


let getData = localStorage.getItem('userProfile') ? JSON.parse(localStorage.getItem('userProfile')) : [];

let isEdit = false;
let editId = null;
showData();


fileInput.onchange = function() {
    if (fileInput.files[0]) {
        if (fileInput.files[0].size < 1000000) {
            const reader = new FileReader();
            reader.onload = function(e) {
                imgPreview.src = e.target.result;
            };
            reader.readAsDataURL(fileInput.files[0]);
        } else {
            alert("El archivo es demasiado grande (máximo 1MB).");
        }
    }
};

function showData() {
    let tableData = "";
    getData.forEach((data, index) => {
        tableData += `<tr>
            <td>${index + 1}</td>
            <td><img src="${data.picture}" alt="Perfil" width="45" height="45" style="object-fit: cover; border-radius: 50%;"></td>
            <td>${data.userName}</td>
            <td>${data.userAge}</td>
            <td>${data.userCity}</td>
            <td>${data.userEmail}</td>
            <td>${data.userPhone}</td>
            <td>${data.userJob}</td>
            <td>${data.userDate}</td>
            <td>
                <button class="btn btn-success" onclick="viewData(${index})"><i class="bi bi-eye"></i></button>
                <button class="btn btn-primary" onclick="editData(${index})"><i class="bi bi-pencil"></i></button>
                <button class="btn btn-danger" onclick="deleteData(${index})"><i class="bi bi-trash3"></i></button>
            </td>
        </tr>`;
    });
    userInfo.innerHTML = tableData;
}



form.addEventListener("submit", (e) => {
    e.preventDefault(); 

    const information = {
        picture: imgPreview.src || "./image/Profile Icon.svg",
        userName: nameInput.value,
        userAge: ageInput.value,
        userCity: cityInput.value,
        userEmail: emailInput.value,
        userPhone: phoneInput.value,
        userJob: jobInput.value,
        userDate: sDateInput.value
    };

    if (!isEdit) {
        getData.push(information);
    } else {
        getData[editId] = information;
        isEdit = false;
    }

    localStorage.setItem("userProfile", JSON.stringify(getData));
    showData();

    // Resetear formulario
    form.reset();
    imgPreview.src = "./image/Profile Icon.svg";
    submitBtn.innerText = "Guardar";
    modalTitle.innerText = "Completa el formulario";

    // Cerrar modal usando la API de Bootstrap
    const modalInstance = bootstrap.Modal.getInstance(userModalEl) || new bootstrap.Modal(userModalEl);
    modalInstance.hide();
});


window.viewData = function(index) {
    const user = getData[index];
    document.getElementById("showImg").src = user.picture;
    document.getElementById("showName").value = user.userName;
    document.getElementById("showAge").value = user.userAge;
    document.getElementById("showCity").value = user.userCity;
    document.getElementById("showEmail").value = user.userEmail;
    document.getElementById("showPhone").value = user.userPhone;
    document.getElementById("showJob").value = user.userJob;
    document.getElementById("showSDate").value = user.userDate;

    const readModal = new bootstrap.Modal(document.getElementById("readData"));
    readModal.show();
};

window.editData = function(index) {
    isEdit = true;
    editId = index;
    const user = getData[index];

    imgPreview.src = user.picture;
    nameInput.value = user.userName;
    ageInput.value = user.userAge;
    cityInput.value = user.userCity;
    emailInput.value = user.userEmail;
    phoneInput.value = user.userPhone;
    jobInput.value = user.userJob;
    sDateInput.value = user.userDate;

    submitBtn.innerText = "Actualizar";
    modalTitle.innerText = "Editar Perfil";

    const editModal = bootstrap.Modal.getInstance(userModalEl) || new bootstrap.Modal(userModalEl);
    editModal.show();
};


window.deleteData = function(index) {
    if (confirm("¿Estás seguro de que quieres eliminar este registro?")) {
        getData.splice(index, 1);
        localStorage.setItem("userProfile", JSON.stringify(getData));
        showData();
    }
};