document.addEventListener('DOMContentLoaded', () => {
  // Star rating
  document.querySelectorAll('.star-rating').forEach(container => {
    const stars = Array.from(container.querySelectorAll('.star'));
    let currentRating = 0;
    function update(r) {
      stars.forEach((s,i) => s.textContent = i < r ? '★' : '☆');
    }
    stars.forEach((star, i) => {
      star.addEventListener('mouseover', () => update(i+1));
      star.addEventListener('mouseout', () => update(currentRating));
      star.addEventListener('click', () => {
        currentRating = i+1;
        update(currentRating);
        console.log(`You selected ${currentRating} stars`);
      });
    });
  });

  // Pagination
  document.querySelectorAll('.pagination .page-link').forEach(link => {
    link.addEventListener('click', e => {
      e.preventDefault();
      const page = link.dataset.page;
      console.log(`Go to page ${page}`);
      document.querySelectorAll('.pagination .page-item')
        .forEach(li => li.classList.remove('active'));
      if (!['prev','next'].includes(page)) {
        link.parentElement.classList.add('active');
      }
    });
  });

  // Navbar active
  const path = window.location.pathname;
  document.querySelectorAll('.navbar .nav-link').forEach(link => {
    if (link.getAttribute('href') === path) {
      link.classList.add('active');
    }
  });

  // Upload form
//  const form = document.getElementById('uploadForm');
//  if (form) {
//    form.addEventListener('submit', e => {
//      e.preventDefault();
//      alert('Recipe submitted!');
//      console.log('Form submit simulated');
//    });
//  }

document.addEventListener('DOMContentLoaded', () => {
  // highlight active nav-link
  const path = window.location.pathname;
  document.querySelectorAll('.nav-link').forEach(link => {
    if (link.getAttribute('href') === path) {
      link.classList.add('active');
    }
  });

  // star rating & pagination omitted here (not used on these pages)
});



});
