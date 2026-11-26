"use strict";


// ========================================================
// === ナビバーの外側をクリックしたらヌルっと閉じる処理 ===
// ========================================================
document.addEventListener('click', (event) => {
	const menu = document.getElementById('nav');
	const toggler = document.querySelector('.navbar-toggler');
	
	if(menu && menu.classList.contains('show')){
		if(!menu.contains(event.target) && !toggler.contains(event.target)){
			
			const collapseInstance = bootstrap.Collapse.getInstance(menu);
			if(collapseInstance){
				collapseInstance.hide();
			}
		}
	}
});