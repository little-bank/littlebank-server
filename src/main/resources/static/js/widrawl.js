// 관리자 시스템 JavaScript

// 기본 설정
const CONFIG = {
    API_URL: 'http://3.34.52.239:8080/',
    //API_URL: 'http://localhost:8080/',
    FONT_WEIGHTS: {
        100: 'Pretendard-Thin',
        200: 'Pretendard-ExtraLight',
        300: 'Pretendard-Light',
        400: 'Pretendard-Regular',
        500: 'Pretendard-Medium',
        600: 'Pretendard-SemiBold',
        700: 'Pretendard-Bold',
        800: 'Pretendard-ExtraBold',
        900: 'Pretendard-Black'
    }
};

// DOM 요소 선택
const elements = {
    pages: document.querySelectorAll('.page'),
    navLinks: document.querySelectorAll('.navigation a, .tabs a'),
    searchInput: document.querySelector('.search-field input'),
    dateFilters: {
        start: document.getElementById('start-date'),
        end: document.getElementById('end-date')
    },
    transactionTypeFilter: document.querySelector('.transaction-type-filter'),
    filterButton: document.querySelector('.filter-options .flat-button'),
    paginationButtons: document.querySelectorAll('.page-btn'),
    notificationBadge: document.querySelector('.notification-badge'),
    actionButtons: document.querySelectorAll('.action-btn'),
    statCards: document.querySelectorAll('.stat-card')
};

function routeFromUrl() {
    const path = window.location.pathname; // ex: /admin/transactions
    const last = path.split('/').pop(); // 'transactions'
    const pageId = `${last}-page`; // 'transactions-page'
    showPage(pageId);
}

const authHeader = localStorage.getItem("adminToken");
const token = authHeader.startsWith("Bearer") ? authHeader : `Bearer ${authHeader}`;

if (!token) {
    alert("로그인이 필요합니다.");
    window.location.href = "/admin/admin-login";
}
console.log("token : {}", token);

async function fetchChargeHistory() {

    if (!token) {
        alert('관리자 인증 토큰이 없습니다. 로그인 후 다시 시도하세요.');
        return;
    }

    try {
        const response = await fetch('/api-admin/point/charge/history', {
            headers: {
                'Authorization': token
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const data = await response.json();
        renderChargeTable(data);
    } catch (error) {
        console.error("충전 내역 로딩 실패:", error);
    }
}

async function fetchRefundHistory() {
    try {
        const response = await fetch('/api-admin/point/refund/history', {
            headers: {
                'Authorization': token // 필요 시 포함
            }
        });

        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        const data = await response.json();
        renderRefundTable(data);
    } catch (error) {
        console.error("환전 내역 로딩 실패:", error);
    }
}

async function processRefund(refundId) {
    try {
        await fetch(`/api-admin/point/refund/sent-money/${refundId}`, {
            method: 'PATCH'
        });
        alert("환전 처리가 완료되었습니다.");
        fetchRefundHistory(); // 목록 새로고침
    } catch (e) {
        alert("환전 처리 중 오류가 발생했습니다.");
        console.error(e);
    }
}

function renderChargeTable(data) {
    const tbody = document.getElementById("charge-table-body");
    tbody.innerHTML = '';

    data.forEach(row => {
        const tr = document.createElement("div");
        tr.className = "table-row";
        tr.innerHTML = `
            <div class="table-cell">${row.userName}</div>
            <div class="table-cell">${row.userEmail}</div>
            <div class="table-cell">${row.amount.toLocaleString()}원</div>
            <div class="table-cell">${row.paymentMethod}</div>
            <div class="table-cell">${row.paidAt ?? '-'}</div>
        `;
        tbody.appendChild(tr);
    });
}

function renderRefundTable(data) {
    const tbody = document.getElementById("refund-table-body");
    tbody.innerHTML = '';

    data.forEach(row => {
        const tr = document.createElement("div");
        tr.className = "table-row";

        const buttonHtml = row.status === 'WAIT'
            ? `<button class="refund-btn" data-id="${row.refundId}">환전</button>`
            : '완료';

        tr.innerHTML = `
            <div class="table-cell">${row.applicationUserName}</div>
            <div class="table-cell">${row.applicationUserEmail}</div>
            <div class="table-cell">${row.requestedAmount.toLocaleString()}원</div>
            <div class="table-cell">${row.requestedAt?.replace('T', ' ') ?? '-'}</div>
            <div class="table-cell">${row.status}</div>
            <div class="table-cell">${row.depositTargetBankName} ${row.depositTargetBankAccount}</div>
            <div class="table-cell">${buttonHtml}</div>
        `;

        tbody.appendChild(tr);
    });

    // 버튼 클릭 이벤트 바인딩
    const buttons = tbody.querySelectorAll(".refund-btn");
    buttons.forEach(btn => {
        btn.addEventListener("click", () => {
            const refundId = btn.dataset.id;
            processRefund(refundId, btn);
        });
    });
}

async function processRefund(refundId, button) {
    if (!confirm("정말로 환전을 완료 처리하시겠습니까?")) return;

    button.disabled = true;
    button.textContent = "처리 중...";

    try {
        const response = await fetch(`/api-admin/point/refund/sent-money/${refundId}`, {
            method: 'PATCH',
            headers: {
                'Authorization': token
            }
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const result = await response.text(); // "환전 처리 완료"
        alert(result);
        fetchRefundHistory(); // ⬅️ UI 새로고침

    } catch (e) {
        alert("환전 처리 중 오류가 발생했습니다.");
        console.error(e);
        button.disabled = false;
        button.textContent = "환전";
    }
}

// 페이지 전환 함수
function showPage(pageId) {
    // 모든 페이지 숨기기
    elements.pages.forEach(page => {
        page.classList.remove('active');
    });

    // 선택된 페이지 보이기
    const targetPage = document.getElementById(pageId);
    if (targetPage) {
        targetPage.classList.add('active');
    }

    // 내비게이션 활성화 상태 업데이트
    elements.navLinks.forEach(link => {
        link.classList.remove('active', 'nav-active');
    });

    // 해당 페이지의 네비게이션 링크 활성화
    const activeLinks = document.querySelectorAll(`[data-page="${pageId.replace('-page', '')}"]`);
    activeLinks.forEach(link => {
        link.classList.add('active');
    });

    // 사이드 네비게이션 활성화
    const sideNavLink = document.querySelector(`.navigation a[data-page="${pageId.replace('-page', '')}"]`);
    if (sideNavLink) {
        sideNavLink.classList.add('nav-active');
    }
}

// 거래 내역 필터링 함수
function filterTransactions() {
    const startDate = elements.dateFilters.start.value;
    const endDate = elements.dateFilters.end.value;
    const transactionType = elements.transactionTypeFilter.value;
    const searchTerm = elements.searchInput ? elements.searchInput.value.toLowerCase() : '';


    // 날짜 필터링
    if (startDate && endDate) {
        filteredTransactions = filteredTransactions.filter(transaction => {
            const transactionDate = new Date(transaction.timestamp);
            return transactionDate >= new Date(startDate) && transactionDate <= new Date(endDate);
        });
    }

    // 거래 유형 필터링
    if (transactionType !== 'all') {
        filteredTransactions = filteredTransactions.filter(transaction =>
            transaction.type === transactionType
        );
    }

    // 검색어 필터링
    if (searchTerm) {
        filteredTransactions = filteredTransactions.filter(transaction =>
            transaction.customerName.toLowerCase().includes(searchTerm) ||
            transaction.accountNumber.includes(searchTerm)
        );
    }

    updateTransactionTable(filteredTransactions);
}

// 거래 내역 테이블 업데이트 함수
function updateTransactionTable(transactions) {
    const tableBody = document.querySelector('.transactions-table .table-body');
    if (!tableBody) return;

    tableBody.innerHTML = '';

    transactions.forEach(transaction => {
        const row = document.createElement('div');
        row.className = 'table-row';

        const typeClass = transaction.type;
        const typeText = {
            'deposit': '입금',
            'withdraw': '출금',
            'transfer': '이체'
        }[transaction.type];

        const amountClass = transaction.amount >= 0 ? 'positive' : 'negative';
        const amountText = transaction.amount >= 0 ?
            `+${transaction.amount.toLocaleString()}원` :
            `${transaction.amount.toLocaleString()}원`;

        const statusClass = transaction.status === 'completed' ? 'success' : 'pending';
        const statusText = transaction.status === 'completed' ? '완료' : '처리중';

        row.innerHTML = `
            <div class="table-cell">${transaction.timestamp}</div>
            <div class="table-cell">${transaction.customerName}</div>
            <div class="table-cell">${transaction.accountNumber}</div>
            <div class="table-cell">
                <span class="transaction-type ${typeClass}">${typeText}</span>
            </div>
            <div class="table-cell amount ${amountClass}">${amountText}</div>
            <div class="table-cell">${transaction.balance.toLocaleString()}원</div>
            <div class="table-cell">
                <span class="status ${statusClass}">${statusText}</span>
            </div>
        `;

        tableBody.appendChild(row);
    });
}




// 반응형 처리 함수
function handleResize() {
    const isMobile = window.innerWidth <= 768;
    const isTablet = window.innerWidth <= 1200;

    if (isMobile) {
        // 모바일 환경에서의 처리
        document.body.classList.add('mobile');
    } else {
        document.body.classList.remove('mobile');
    }

    if (isTablet) {
        // 태블릿 환경에서의 처리
        document.body.classList.add('tablet');
    } else {
        document.body.classList.remove('tablet');
    }
}

// 이벤트 리스너 설정
function setupEventListeners() {
    // 페이지 내비게이션
    elements.navLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const pageId = link.getAttribute('data-page');
            if (pageId) {
                showPage(pageId + '-page');
                window.history.pushState({}, '', `/admin/${pageId}`); // 주소 변경만 (선택)
            } else if (link.href.includes('#')) {
                // 대시보드 홈 링크
                showPage('dashboard-page');
            }
        });
    });

    // 필터 버튼
    if (elements.filterButton) {
        elements.filterButton.addEventListener('click', filterTransactions);
    }

    // 검색 입력
    if (elements.searchInput) {
        elements.searchInput.addEventListener('input', (e) => {
            filterTransactions();
        });
    }

    // 날짜 필터
    Object.values(elements.dateFilters).forEach(filter => {
        if (filter) {
            filter.addEventListener('change', filterTransactions);
        }
    });

    // 거래 유형 필터
    if (elements.transactionTypeFilter) {
        elements.transactionTypeFilter.addEventListener('change', filterTransactions);
    }

    // 페이지네이션
    elements.paginationButtons.forEach(button => {
        button.addEventListener('click', (e) => {
            e.preventDefault();
            elements.paginationButtons.forEach(btn => btn.classList.remove('active'));
            button.classList.add('active');
            // 여기에 페이지 로딩 로직 추가
        });
    });

    // 빠른 작업 버튼
    elements.actionButtons.forEach(button => {
        button.addEventListener('click', (e) => {
            e.preventDefault();
            const buttonText = button.textContent.trim();

            switch (buttonText) {
                case '데이터 다운로드':
                    showPage('transactions-page');
                    break;
                case '보고서 생성':
                    showPage('reports-page');
                    break;
                case '통계 분석':
                    showPage('dashboard-page');
                    break;
                case '설정':
                    showPage('settings-page');
                    break;
            }
        });
    });

    // 윈도우 리사이즈
    window.addEventListener('resize', handleResize);

    // 설정 탭 전환
    const tabButtons = document.querySelectorAll('.tab-button');
    const tabPanels = document.querySelectorAll('.tab-panel');

    tabButtons.forEach(button => {
        button.addEventListener('click', () => {
            const targetTab = button.getAttribute('data-tab');

            // 모든 탭 버튼과 패널에서 active 클래스 제거
            tabButtons.forEach(btn => btn.classList.remove('active'));
            tabPanels.forEach(panel => panel.classList.remove('active'));

            // 선택된 탭 버튼과 패널에 active 클래스 추가
            button.classList.add('active');
            const targetPanel = document.getElementById(targetTab);
            if (targetPanel) {
                targetPanel.classList.add('active');
            }
        });
    });
}

// 기존 컨텍스트 메뉴 기능 유지
const $menu = document.getElementById('menu');
const $li = $menu ? $menu.querySelectorAll('li') : [];
const $hue1 = document.querySelector('#h1');
const $hue2 = document.querySelector('#h2');

let cleanTimer;

// 컨텍스트 메뉴 이벤트
if ($menu) {
    document.addEventListener("contextmenu", (event) => {
        const menuBox = $menu.getBoundingClientRect();
        const bodyBox = { width: window.innerWidth, height: window.innerHeight };
        const target = { x: event.clientX, y: event.clientY };
        const padding = { x: 30, y: 20 };

        const hitX = target.x + menuBox.width >= bodyBox.width - padding.x;
        const hitY = target.y + menuBox.height >= bodyBox.height - padding.y;

        if (hitX) {
            target.x = bodyBox.width - menuBox.width - padding.x;
        }

        if (hitY) {
            target.y = bodyBox.height - menuBox.height - padding.y;
        }

        const $target = event.target;
        const isMenu = $menu.contains($target);
        event.preventDefault();

        if (!isMenu) {
            $menu.style.left = target.x + 'px';
            $menu.style.top = target.y + 'px';
            $menu.classList.add('open');
            clearTimeout(cleanTimer);
        }
    });

    document.addEventListener('pointerdown', (event) => {
        const $target = event.target;
        const isMenu = $menu.contains($target);
        const isSlider = $target.matches('input');

        if (!isMenu && !isSlider) {
            $menu.classList.remove('open');
            cleanTimer = setTimeout(() => {
                const menuInput = $menu.querySelector('input');
                if (menuInput) menuInput.value = '';
                $li.forEach($el => {
                    $el.classList.remove('selected');
                });
            }, 200);
        } else if (isMenu) {
            $li.forEach($el => {
                $el.classList.remove('selected');
            });
            if ($target.matches('li')) {
                $target.classList.add('selected');
            }
        }
    });
}

// 색상 슬라이더 이벤트
if ($hue1) {
    $hue1.addEventListener('input', (event) => {
        requestAnimationFrame(() => {
            document.body.style.setProperty('--hue1', event.target.value);
            if ($menu) $menu.classList.add('open');
        });
    });
}

if ($hue2) {
    $hue2.addEventListener('input', (event) => {
        requestAnimationFrame(() => {
            document.body.style.setProperty('--hue2', event.target.value);
            if ($menu) $menu.classList.add('open');
        });
    });
}

// 초기 색상 설정
if ($hue1 && $hue2) {
    const rand1 = 120 + Math.floor(Math.random() * 240);
    const rand2 = rand1 - 80 + (Math.floor(Math.random() * 60) - 30);
    $hue1.value = rand1;
    $hue2.value = rand2;
    document.body.style.setProperty('--hue1', rand1);
    document.body.style.setProperty('--hue2', rand2);
}

// 초기화 함수
function init() {
    setupEventListeners();
    // updateNotifications();
    handleResize();
    routeFromUrl();
    fetchChargeHistory();
    fetchRefundHistory();
    // 기본 페이지 설정
    showPage('dashboard-page');

    console.log('LittleBank 결제 관리 시스템이 초기화되었습니다.');
}

// DOM 로드 완료 후 초기화
document.addEventListener('DOMContentLoaded', init);

// 모듈 내보내기 (필요한 경우)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        showPage,
        updateStatsCards,
        CONFIG
    };
}