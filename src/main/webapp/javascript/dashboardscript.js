/* Search Bar ---------------------------------------------------------------------*/
document.addEventListener('DOMContentLoaded', () => {
    const searchBar = document.getElementById('search-bar');
    const searchResults = document.getElementById('search-results');


    /* fixed search terms can add stuff in here, and a link  ---------------------------------------------------------------------*/
    const items = [
        { name: 'Home', link: '#home' },
        { name: 'Assignments', link: '#assignments' },
        { name: 'Notifications', link: '#notifications' },
        { name: 'Classes', link: '#classes' },
        { name: 'Lessons', link: '#lessons' },
        { name: 'Profile', link: '#profile' },
        { name: 'People', link: '#people' },
        { name: 'Help', link: '#help' },

        { name: 'Add Goal', link: '#addGoal' },
        { name: 'Logout', link: '#logout' },
        { name: 'Split Homework', link: '#splitHW' },
        { name: 'Create Assignment', link: '#createAssignment' },
        { name: 'Class 12-A', link: '#classLink' },
        { name: 'Class 12-B', link: '#classLink' },
        { name: 'Class 11-C', link: '#classLink' }

    ];

    searchBar.addEventListener('input', () => {
        const query = searchBar.value.toLowerCase();
        searchResults.innerHTML = '';

        if (query) {
            const filteredItems = items.filter(item => item.name.toLowerCase().includes(query));

            if (filteredItems.length > 0) {
                searchResults.style.display = 'block';
                filteredItems.forEach(item => {
                    const resultItem = document.createElement('div');
                    resultItem.textContent = item.name;
                    resultItem.addEventListener('click', () => {
                        window.location.href = item.link;
                    });
                    searchResults.appendChild(resultItem);
                });
            } else {
                searchResults.style.display = 'none';
            }
        } else {
            searchResults.style.display = 'none';
        }
    });


    document.addEventListener('click', (event) => {
        if (!event.target.closest('.user-settings')) {
            searchResults.style.display = 'none';
        }
    });
});


/* Calender Views ---------------------------------------------------------------------*/


document.addEventListener('DOMContentLoaded', () => {
    const monthViewButton = document.getElementById('month-view-button');
    const weekViewButton = document.getElementById('week-view-button');
    const dayViewButton = document.getElementById('day-view-button');

    const monthView = document.querySelector('.calendar-grid.month-view');
    const weekView = document.querySelector('.calendar-grid.week-view');
    const dayView = document.querySelector('.calendar-grid.day-view');

    monthViewButton.addEventListener('click', () => {
        monthView.style.display = 'grid';
        weekView.style.display = 'none';
        dayView.style.display = 'none';
    });

    weekViewButton.addEventListener('click', () => {
        monthView.style.display = 'none';
        weekView.style.display = 'table';
        dayView.style.display = 'none';
    });

    dayViewButton.addEventListener('click', () => {
        monthView.style.display = 'none';
        weekView.style.display = 'none';
        dayView.style.display = 'block';
    });


    monthView.style.display = 'grid';
    weekView.style.display = 'none';
    dayView.style.display = 'none';
});


