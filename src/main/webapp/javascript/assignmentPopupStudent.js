
/* Pop-Ups ---------------------------------------------------------------------*/

document.addEventListener("DOMContentLoaded", function() {
    const splitBtn = document.querySelector(".split-btn");
    const splitPopUp = document.querySelector(".split-pop-up");
    const splitOverlay = document.querySelector(".split-pop-up-overlay");

    splitBtn.addEventListener("click", function() {
        splitPopUp.classList.toggle("visible");
        goalsOverlay.classList.toggle("visible");
    });

    splitOverlay.addEventListener("click", function() {
        splitPopUp.classList.remove("visible");
        splitOverlay.classList.remove("visible");
    });
});