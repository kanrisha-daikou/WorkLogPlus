"use strict";


// 🔑 【追加】HTMLに埋め込んだmetaタグからCSRFトークン情報を自動で読み取る
const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute("content");
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute("content");

let myToast = null;

// トーストの文字を(message)の内容で表示させる共通関数
function showSuccessToast(message){
	const toastMessage =document.getElementById('toastMessage');
	if(toastMessage){
		toastMessage.textContent = message;
	}
	
	const toastElement = document.getElementById('successToast');
	if(!myToast){
		myToast = new bootstrap.Toast(toastElement, {delay: 2000});
	}
	myToast.show();
}



// 各タブ内の数（バッジ）を最新の数に更新する共通関数
function updateTabBadges(){
	const allSpan = document.querySelector('#taskTabList button[data-status="all"] span');
	const todoSpan = document.querySelector('#taskTabList button[data-status="TODO"] span');
	const doingSpan = document.querySelector('#taskTabList button[data-status="DOING"] span');
	const doneSpan = document.querySelector('#taskTabList button[data-status="DONE"] span');
	
	const todoCount = document.querySelectorAll('#task-container .task-section[data-section-status="TODO"] .card').length;
	const doingCount = document.querySelectorAll('#task-container .task-section[data-section-status="DOING"] .card').length;
	const doneCount = document.querySelectorAll('#task-container .task-section[data-section-status="DONE"] .card').length;
	
	const allCount = todoCount + doingCount + doneCount;
	
	if(allSpan) allSpan.textContent = allCount;
	if(todoSpan) todoSpan.textContent = todoCount;
	if(doingSpan) doingSpan.textContent = doingCount;
	if(doneSpan) doneSpan.textContent = doneCount;
}



// 各タブの中身を調べ、空の場合にメッセージを出す為の関数
function updateEmptyMessages(){
	
	document.querySelectorAll('#task-container .task-section').forEach(section => {
		const cardCount = section.querySelectorAll('[id^="task-card-"]').length;
		const emptyMessage = section.querySelector('.empty-message');
		
		if(emptyMessage){
			if(cardCount === 0){
				emptyMessage.classList.remove('d-none');
			
			}else{
				emptyMessage.classList.add('d-none');
			}
		}
	});
}



//* レスポンスのエラーチェックを共通で判定する関数
async function isResponseSuccess(response){
	
	if(response.ok){
		return true;
	}
	
	if(response.status === 401){
		alert("セッションの有効期限が切れました。再ログインしてください。");
		window.location.href = "/login";
		return false;
	}
	
	try{
		const errorData = await response.json(); // json()も非同期処理
		alert(errorData.detail || "処理に失敗しました。"); // 三項演算子みたいな仕組み
	}catch(err){
		console.error("JSONのパースに失敗しました（HTMLが返ってきた可能性があります）:", err);
		alert("エラーが発生しました。時間を置いて再度お試しください。");
	}
	
	return false;
}



//--------------- タイトル更新 ---------------
// ① タイトルをクリックした時：文字を隠して「入力欄」を出す
function enableTitleEdit(element){
	const container = element.closest('.task-title-container');
	const input = container.querySelector('.task-title-input');
	
	element.classList.add('d-none');
	input.classList.remove('d-none');
	input.focus();
}


// ② タイトル入力欄からフォーカスが外れた時
// （またはEnterが押された時）：Javaへ非同期通信
async function saveTitleEdit(input){
	const container = input.closest('.task-title-container');
	const textElement = container.querySelector('.task-title-text');
	const taskId = container.getAttribute('data-task-id');
	const newTitle = input.value.trim();
	
	if(newTitle === "" || newTitle === textElement.textContent){
		input.value = textElement.textContent; // 元に戻す
		input.classList.add('d-none');
		textElement.classList.remove('d-none');
		return;
	}
	
	try{
		const response = await fetch(
			`/tasks/${taskId}/update-title?title=${encodeURIComponent(newTitle)}`, {
				method: 'POST',
				// 🔑 ヘッダー情報を設定し、CSRFトークンを載せる
				headers:{[csrfHeader]: csrfToken}
			}
		);
		
		//	エラーチェック
		if(!(await isResponseSuccess(response))){
			input.value = textElement.textContent;
			return;
		}
		
		// ---- 【成功時（true）の固有処理】 ----
		showSuccessToast("タイトルを更新しました");
		textElement.textContent = newTitle; // JavaScriptが今ある画面の文字を書き換える！

		textElement.classList.add('text-success'); // 文字を緑色にする
		setTimeout(() => {
		    textElement.classList.remove('text-success'); // 0.4秒後に元の色に戻す
		}, 400);
		
	} catch(error) {
		console.error('Error:' , error);
		alert("通信エラーが発生しました。");
		input.value = textElement.textContent;
	} finally {
		// 最後に、入力欄を隠して文字表示に戻す
		input.classList.add('d-none');
		textElement.classList.remove('d-none');
	}
}


// ③ 入力欄でEnterキーが押された時、タイトルからフォーカスを外す
function checkTitleEnter(event, input){
	if(event.key === 'Enter'){
		input.blur(); // フォーカスを外すので、②のsaveTitleEditへ
	}
}



//--------------- ステータス更新 ---------------//
async function updateStatus(taskId, status){
	
	const params = new URLSearchParams();
	params.append('status', status);
	
	try{
		const response = await fetch(
			`/tasks/${taskId}/update-status`, {
				method: 'POST',
				headers: {'Content-Type': 'application/x-www-form-urlencoded', [csrfHeader]: csrfToken},
				body: params
			}
		);
		
		if(!(await isResponseSuccess(response))){
			return;
		}
		
		showSuccessToast("ステータスを更新しました");
		
		const taskCard = document.getElementById(`task-card-${taskId}`);
		const targetSection = document.querySelector(`#task-container .task-section[data-section-status="${status}"]`);
		
		if(taskCard && targetSection){
			targetSection.appendChild(taskCard);
			
			updateTabBadges();
			
			updateEmptyMessages();
		}
		
	} catch(error) {
		console.error('Error:', error);
		alert('通信エラーが発生しました。');
	}
}



//--------------- 期限更新 ---------------//
async function updateDueDate(taskId){
	const dateInput = document.getElementById(`dateInput-${taskId}`)
	const selectedDate = dateInput.value;
	
	if(!selectedDate){
		alert("日付を選択してください。");
		return;
	}
	
	const params = new URLSearchParams();
	params.append('dueDate', selectedDate);
	
	try{
		const response = await fetch(
			`/tasks/${taskId}/update-duedate`, {
				method: 'POST',
				headers: {'Content-Type': 'application/x-www-form-urlencoded', [csrfHeader]: csrfToken},
				body: params
			}
		);
		
		if(!(await isResponseSuccess(response))){
			return;
		}
		
		// ---- 【成功時の固有処理】 ----
		const formattedDate = selectedDate.replace(/-/g, '/');
		const dateDisplay = document.getElementById(`dateDisplay-${taskId}`);
		const btnText = document.getElementById(`btnText-${taskId}`);
		if(dateDisplay){
			dateDisplay.textContent = formattedDate;
			dateDisplay.classList.toggle('d-none', false);
		}

		if(btnText){
			btnText.classList.toggle('d-none', true);
		}
		showSuccessToast("期限を更新しました");

		const modalElement = document.getElementById(`editDateModal-${taskId}`);
		const modal = bootstrap.Modal.getInstance(modalElement);
		modal.hide();
		
	} catch(error) {
		console.error('Error:', error);
		alert('通信エラーが発生しました。')
	}
}



//--------------- タスクを削除 ---------------//
async function deleteTask(taskId){
	if(!confirm('このタスクを削除してもよろしいですか？')){
		return;
	}
	
	try{
		const response = await fetch(
			`/tasks/${taskId}/delete`, {
				method: 'POST',
				headers:{[csrfHeader]: csrfToken}
			}
		);
		
		if(!(await isResponseSuccess(response))){
			return;
		}
		
		// ---- 【成功時の固有処理】 ----
		const taskCard = document.getElementById(`task-card-${taskId}`);
		if(taskCard){
			taskCard.classList.remove('show');
			
			setTimeout(() => {
				taskCard.remove();
				updateTabBadges()
			}, 150);
		}
		showSuccessToast('タスクを削除しました');
		
	} catch(error) {
		console.error('Error:', error);
		alert('エラーが発生しました。');
	}
}



//--------------- スマホ用タブ切り替え ---------------//
document.addEventListener('DOMContentLoaded', () => {
	const tabButtons = document.querySelectorAll('#taskTabList .nav-link');
	const sections = document.querySelectorAll('#task-container .task-section');
	
	if(tabButtons.length === 0) return;
	
	tabButtons.forEach(button => {
		button.addEventListener('click', () => {
			// ① すべてのタブボタンからactiveを消し、クリックしたボタンだけに付ける
			tabButtons.forEach(btn => btn.classList.remove('active'));
			button.classList.add('active');
			
			// ② タブボタンに設定されているステータス（all, TODO, DOING, DONE）を取得
			const selectedStatus = button.getAttribute('data-status');

			// ③ タブボタンのdata-statusに合わせてセクションの表示・非表示をパチパチ切り替える
			sections.forEach(section => {
				const sectionStatus = section.getAttribute('data-section-status');
				
				if(selectedStatus === 'all' || selectedStatus === sectionStatus){
					// 「すべて」が選ばれたか、ボタンのステータスと一致するセクションは表示
					section.classList.remove('d-none');
				}else{
					section.classList.add('d-none');
				}
			});
		});
	});
	
	
	
	// ======================================================================
	// === ダッシュボードのステータス件数をクリックして遷移してきたときに ===
	// === そのステータスをアクティブにするためのやつ                     ===
	// ======================================================================
	const urlParams = new URLSearchParams(window.location.search);
	const statusParam = urlParams.get('status');
	
	// if文を使わず、安全に1行で書く方法（ ?. ←オプショナルチェイニング演算子）
	document.querySelector(`#taskTabList button[data-status="${statusParam}"]`)?.click();
	
	//	イフ文で書く方法だとこういうこと
	//	if(statusParam){
	//		const targetButton = document.querySelector(`#taskTabList button[data-status="${statusParam}"]`);
	//		
	//		if(targetButton){
	//			targetButton.click();
	//		}
	//	}
	
});
