// toast.js
console.log("toast.js loaded");
(function () {
    function ensureToastEl() {
        let el = document.getElementById("toast");
        if (!el) {
            el = document.createElement("div");
            el.id = "toast";
            el.className = "toast";
            el.setAttribute("aria-live", "polite");
            el.setAttribute("aria-atomic", "true");
            document.body.appendChild(el);
        }
        return el;
    }

    window.showToast = function (message, type = "default", ms = 3000) {
        const el = ensureToastEl();

        // reset types
        el.classList.remove("success", "error", "warn");
        if (type && type !== "default") el.classList.add(type);

        el.textContent = message;
        el.classList.add("show");

        clearTimeout(el._t);
        el._t = setTimeout(() => {
            el.classList.remove("show");
        }, ms);
    };
})();
