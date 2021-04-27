window.onscroll = function() {scrollFunction()};
function scrollFunction() {
    if (document.body.scrollTop > 20 || document.documentElement.scrollTop > 20) {
        document.getElementById("idNav").classList.add("nav_active");
    } else {
        document.getElementById("idNav").classList.remove("nav_active");
    }
}

function openMenu() {
    document.getElementById("nav__list").classList.add("nav__list-active");
}

$(document).ready(function(){
    $('.menu-btn').click(function(){
        $('.nav__list').toggleClass("active");
        $('.menu-btn i').toggleClass("active");
    })
});


function closeMenu() {
    document.getElementById("nav__list").classList.remove("active");
    $('.menu-btn i').toggleClass("active");
}

var typed = new Typed(".typing", {
    strings: ["student", "web developer", "android developer", "freelancer"],
    typeSpeed: 100,
    backSpeed: 60,
    loop: true
});