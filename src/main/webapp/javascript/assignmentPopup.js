/* Pop-Ups ---------------------------------------------------------------------*/

document.addEventListener("DOMContentLoaded", function() {
    const addGoalBtn = document.querySelector(".add-goal-btn");
    const goalsPopUp = document.querySelector(".goals-pop-up");
    const goalsOverlay = document.querySelector(".goals-pop-up-overlay");

    const splitBtn = document.querySelector(".split-btn");
    const splitPopUp = document.querySelector(".split-pop-up");
    const splitOverlay = document.querySelector(".split-pop-up-overlay");

    if (addGoalBtn && goalsPopUp && goalsOverlay) {
        addGoalBtn.addEventListener("click", function() {
            goalsPopUp.classList.toggle("visible");
            goalsOverlay.classList.toggle("visible");
        });

        goalsOverlay.addEventListener("click", function() {
            goalsPopUp.classList.remove("visible");
            goalsOverlay.classList.remove("visible");
        });
    }

    if (splitBtn && splitPopUp && splitOverlay) {
        splitBtn.addEventListener("click", function() {
            splitPopUp.classList.toggle("visible");
            goalsOverlay.classList.toggle("visible");
        });

        splitOverlay.addEventListener("click", function() {
            splitPopUp.classList.remove("visible");
            splitOverlay.classList.remove("visible");
        });
    }
});
