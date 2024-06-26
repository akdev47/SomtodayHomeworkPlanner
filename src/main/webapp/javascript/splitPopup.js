
/* Pop-Ups ---------------------------------------------------------------------*/

document.addEventListener('DOMContentLoaded', () => {
    const splitBtn = document.querySelector('.split-btn');
    const splitPopUp = document.querySelector('.split-pop-up');
    const splitOverlay = document.querySelector('.split-pop-up-overlay');
    const scSubmitBtn = document.querySelector('.sc-submit-btn');
    const individualSplitPopUp = document.querySelector('.individual-split');
    const individualSplitOverlay = document.querySelector('.individual-split-overlay');
    const splitForm = document.getElementById('splitForm');
    const splitCountInput = document.getElementById('splitCount');
    const individualSplitForm = document.getElementById('individualSplitForm');

    let currentSplitCount = 0;
    let totalSplits = 0;

    // Show the first pop-up
    splitBtn.addEventListener('click', () => {
        splitPopUp.style.display = 'block';
        splitOverlay.style.display = 'block';
    });

    // Handle form submission for the first pop-up
    splitForm.addEventListener('submit', (e) => {
        e.preventDefault(); // Prevent form submission
        totalSplits = parseInt(splitCountInput.value); // Get the number of splits
        if (totalSplits > 0) {
            currentSplitCount = 1;
            splitPopUp.style.display = 'none'; // Hide the first pop-up
            splitOverlay.style.display = 'none';
            showIndividualSplitPopUp(); // Show the first individual split pop-up
        }
    });

    // Function to show the individual split pop-up
    function showIndividualSplitPopUp() {
        individualSplitPopUp.style.display = 'block';
        individualSplitOverlay.style.display = 'block';
    }

    // Handle form submission for the individual split pop-up
    individualSplitForm.addEventListener('submit', (e) => {
        e.preventDefault(); // Prevent form submission
        currentSplitCount++;
        if (currentSplitCount > totalSplits) {
            individualSplitPopUp.style.display = 'none'; // Hide the second pop-up
            individualSplitOverlay.style.display = 'none';
        } else {
            individualSplitForm.reset(); // Reset the form for the next input
        }
    });
});
