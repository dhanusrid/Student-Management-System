/**
 * app.js - Student Management System Frontend Logic
 * 
 * Explains basic JavaScript concepts:
 * 1. Fetch API to send GET and POST requests to Java backend
 * 2. Event Listeners for buttons and form submissions
 * 3. DOM Manipulation to dynamically update tables, stats, and modals
 */

// Global State
let studentsList = [];
let deleteTargetId = null;

// Base URL for the Java Servlet / Server API
const API_BASE = '/api/students';

// Initialize application on DOM load
document.addEventListener('DOMContentLoaded', () => {
    setupEventListeners();
    loadStudents();
});

/**
 * Attaches event listeners to buttons, forms, and navigation links.
 */
function setupEventListeners() {
    // Navigation Links
    document.querySelectorAll('[data-view]').forEach(element => {
        element.addEventListener('click', (e) => {
            e.preventDefault();
            const viewName = element.getAttribute('data-view');
            switchView(viewName);
        });
    });

    // Mobile Sidebar Toggle
    const mobileMenuBtn = document.getElementById('mobileMenuBtn');
    if (mobileMenuBtn) {
        mobileMenuBtn.addEventListener('click', toggleSidebar);
    }

    // Add Student Form Submission
    const addStudentForm = document.getElementById('addStudentForm');
    if (addStudentForm) {
        addStudentForm.addEventListener('submit', handleAddStudent);
    }

    // Edit Student Form Submission
    const editStudentForm = document.getElementById('editStudentForm');
    if (editStudentForm) {
        editStudentForm.addEventListener('submit', handleUpdateStudent);
    }

    // Delete Confirmation Button
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener('click', executeDeleteStudent);
    }

    // Search and Filters (Instant Live Search)
    const tableSearchInput = document.getElementById('tableSearchInput');
    if (tableSearchInput) {
        tableSearchInput.addEventListener('input', filterStudentsTable);
    }

    const tableDeptFilter = document.getElementById('tableDeptFilter');
    if (tableDeptFilter) {
        tableDeptFilter.addEventListener('change', filterStudentsTable);
    }

    const tableYearFilter = document.getElementById('tableYearFilter');
    if (tableYearFilter) {
        tableYearFilter.addEventListener('change', filterStudentsTable);
    }

    // Dedicated Search View Elements
    const searchForm = document.getElementById('dedicatedSearchForm');
    if (searchForm) {
        searchForm.addEventListener('submit', handleDedicatedSearch);
    }

    const resetSearchBtn = document.getElementById('resetSearchBtn');
    if (resetSearchBtn) {
        resetSearchBtn.addEventListener('click', resetDedicatedSearch);
    }
}

/**
 * Switches between views (Home, Dashboard, View Students, Add Student, Search).
 */
function switchView(viewName) {
    // Hide all view panels
    document.querySelectorAll('.view-panel').forEach(panel => {
        panel.classList.remove('active');
    });

    // Show target view panel
    const targetPanel = document.getElementById(`view-${viewName}`);
    if (targetPanel) {
        targetPanel.classList.add('active');
    }

    // Update active class in sidebar navigation
    document.querySelectorAll('.nav-item').forEach(item => {
        item.classList.remove('active');
        if (item.getAttribute('data-view') === viewName) {
            item.classList.add('active');
        }
    });

    // Update Topbar Title and Description
    const pageTitle = document.getElementById('pageTitle');
    const pageDesc = document.getElementById('pageDesc');

    const titles = {
        'home': { title: 'College Overview', desc: 'Welcome to the Student Management System' },
        'dashboard': { title: 'Student Analytics Dashboard', desc: 'Real-time metrics, department distribution, and student statistics' },
        'view-students': { title: 'All Registered Students', desc: 'View, edit, search, and manage complete student records' },
        'add-student': { title: 'Register New Student', desc: 'Add a new student profile with official college credentials' },
        'search-student': { title: 'Search Student Database', desc: 'Find student profiles by name, register number, department, or year' }
    };

    if (titles[viewName]) {
        pageTitle.textContent = titles[viewName].title;
        pageDesc.textContent = titles[viewName].desc;
    }

    // Close mobile sidebar if open
    const sidebar = document.getElementById('sidebar');
    if (sidebar && sidebar.classList.contains('open')) {
        sidebar.classList.remove('open');
    }

    // Refresh data if switching to table or dashboard
    if (viewName === 'dashboard' || viewName === 'view-students') {
        loadStudents();
    }
}

/**
 * Toggles the mobile responsive sidebar.
 */
function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    if (sidebar) {
        sidebar.classList.toggle('open');
    }
}

/**
 * READ OPERATION: Fetches all students from the Java backend using Fetch API.
 */
async function loadStudents() {
    try {
        const response = await fetch(API_BASE);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        const data = await response.json();
        studentsList = Array.isArray(data) ? data : [];

        // Render both views
        renderStudentsTable(studentsList, 'studentsTableBody');
        renderDashboard(studentsList);
        updateCounters(studentsList);
    } catch (error) {
        console.error('Error fetching students:', error);
        showToast('Could not connect to Java backend. Please check server.', 'error');
    }
}

/**
 * Updates quick counters on Home and Dashboard
 */
function updateCounters(list) {
    const homeTotal = document.getElementById('homeTotalStudents');
    if (homeTotal) homeTotal.textContent = list.length;
}

/**
 * Renders the Analytics Dashboard with metric cards, department breakdown, and recent records.
 */
function renderDashboard(list) {
    // 1. Calculate Metrics
    const totalStudents = list.length;

    // Unique departments
    const departments = {};
    const years = { '1st Year': 0, '2nd Year': 0, '3rd Year': 0, '4th Year': 0 };

    list.forEach(s => {
        // Department count
        if (s.department) {
            departments[s.department] = (departments[s.department] || 0) + 1;
        }
        // Year count
        if (s.yearOfStudy && years[s.yearOfStudy] !== undefined) {
            years[s.yearOfStudy]++;
        }
    });

    const totalDepts = Object.keys(departments).length;

    // Update Stat Cards
    const statTotal = document.getElementById('dashTotalStudents');
    const statDepts = document.getElementById('dashTotalDepts');
    const statFinalYear = document.getElementById('dashFinalYear');
    const statFirstYear = document.getElementById('dashFirstYear');

    if (statTotal) statTotal.textContent = totalStudents;
    if (statDepts) statDepts.textContent = totalDepts;
    if (statFinalYear) statFinalYear.textContent = years['4th Year'] || 0;
    if (statFirstYear) statFirstYear.textContent = years['1st Year'] || 0;

    // 2. Render Department Distribution Bars
    const deptListContainer = document.getElementById('dashboardDeptList');
    if (deptListContainer) {
        deptListContainer.innerHTML = '';
        const deptKeys = Object.keys(departments);

        if (deptKeys.length === 0) {
            deptListContainer.innerHTML = '<p class="text-muted">No department data available.</p>';
        } else {
            deptKeys.forEach(dept => {
                const count = departments[dept];
                const percentage = totalStudents > 0 ? Math.round((count / totalStudents) * 100) : 0;
                const fillClass = getDeptFillClass(dept);

                const item = document.createElement('div');
                item.className = 'dept-item';
                item.innerHTML = `
                    <div class="dept-header">
                        <span>${escapeHtml(dept)}</span>
                        <span>${count} (${percentage}%)</span>
                    </div>
                    <div class="dept-bar-bg">
                        <div class="dept-bar-fill ${fillClass}" style="width: ${percentage}%"></div>
                    </div>
                `;
                deptListContainer.appendChild(item);
            });
        }
    }

    // 3. Render Recent 5 Students Table
    const recentStudents = [...list].slice(0, 5);
    renderStudentsTable(recentStudents, 'dashboardRecentTableBody', true);
}

/**
 * Renders student rows into a target table body.
 */
function renderStudentsTable(list, tbodyId, isCompact = false) {
    const tbody = document.getElementById(tbodyId);
    if (!tbody) return;

    tbody.innerHTML = '';

    if (!list || list.length === 0) {
        const colSpan = isCompact ? 5 : 8;
        tbody.innerHTML = `
            <tr>
                <td colspan="${colSpan}">
                    <div class="empty-state">
                        <div class="empty-state-icon">📋</div>
                        <h3>No Student Records Found</h3>
                        <p>No records matched the current selection or search criteria.</p>
                    </div>
                </td>
            </tr>
        `;
        return;
    }

    list.forEach((s, index) => {
        const tr = document.createElement('tr');
        const badgeColor = getDeptBadgeColor(s.department);
        const initials = getInitials(s.name);

        if (isCompact) {
            tr.innerHTML = `
                <td><span class="reg-no-code">${escapeHtml(s.regNo)}</span></td>
                <td>
                    <div class="student-name-cell">
                        <div class="avatar-badge">${initials}</div>
                        <span>${escapeHtml(s.name)}</span>
                    </div>
                </td>
                <td><span class="badge ${badgeColor}">${escapeHtml(s.department)}</span></td>
                <td><span class="badge badge-year">${escapeHtml(s.yearOfStudy)}</span></td>
                <td>
                    <button class="btn-icon btn-edit" onclick="openEditModal(${s.id})" title="Edit Student">✏️</button>
                </td>
            `;
        } else {
            tr.innerHTML = `
                <td>${index + 1}</td>
                <td><span class="reg-no-code">${escapeHtml(s.regNo)}</span></td>
                <td>
                    <div class="student-name-cell">
                        <div class="avatar-badge">${initials}</div>
                        <span>${escapeHtml(s.name)}</span>
                    </div>
                </td>
                <td>${escapeHtml(s.email)}</td>
                <td>${escapeHtml(s.phone)}</td>
                <td><span class="badge ${badgeColor}">${escapeHtml(s.department)}</span></td>
                <td><span class="badge badge-year">${escapeHtml(s.yearOfStudy)}</span></td>
                <td>
                    <div class="action-buttons">
                        <button class="btn-icon btn-edit" onclick="openEditModal(${s.id})" title="Edit Student">✏️</button>
                        <button class="btn-icon btn-delete" onclick="openDeleteModal(${s.id}, '${escapeQuote(s.name)}', '${escapeQuote(s.regNo)}')" title="Delete Student">🗑️</button>
                    </div>
                </td>
            `;
        }
        tbody.appendChild(tr);
    });
}

/**
 * Filter table rows dynamically based on search text and dropdowns
 */
function filterStudentsTable() {
    const searchInput = document.getElementById('tableSearchInput');
    const deptFilter = document.getElementById('tableDeptFilter');
    const yearFilter = document.getElementById('tableYearFilter');

    const query = searchInput ? searchInput.value.trim().toLowerCase() : '';
    const dept = deptFilter ? deptFilter.value : '';
    const year = yearFilter ? yearFilter.value : '';

    const filtered = studentsList.filter(s => {
        const matchQuery = !query ||
            (s.name && s.name.toLowerCase().includes(query)) ||
            (s.regNo && s.regNo.toLowerCase().includes(query)) ||
            (s.email && s.email.toLowerCase().includes(query));

        const matchDept = !dept || dept === 'All' || (s.department && s.department === dept);
        const matchYear = !year || year === 'All' || (s.yearOfStudy && s.yearOfStudy === year);

        return matchQuery && matchDept && matchYear;
    });

    renderStudentsTable(filtered, 'studentsTableBody');
}

/**
 * CREATE OPERATION: Handles Add Student form submission via POST
 */
async function handleAddStudent(e) {
    e.preventDefault();

    const name = document.getElementById('addName').value.trim();
    const regNo = document.getElementById('addRegNo').value.trim();
    const email = document.getElementById('addEmail').value.trim();
    const phone = document.getElementById('addPhone').value.trim();
    const department = document.getElementById('addDepartment').value;
    const yearOfStudy = document.getElementById('addYearOfStudy').value;

    // Client-side Validation
    if (!name || !regNo || !email || !phone || !department || !yearOfStudy) {
        showToast('Please fill out all required fields!', 'error');
        return;
    }

    if (!validateEmail(email)) {
        showToast('Please enter a valid email address!', 'error');
        return;
    }

    if (!validatePhone(phone)) {
        showToast('Please enter a valid phone number (at least 10 digits)!', 'error');
        return;
    }

    // Prepare URL-encoded form body
    const params = new URLSearchParams();
    params.append('action', 'add');
    params.append('name', name);
    params.append('regNo', regNo);
    params.append('email', email);
    params.append('phone', phone);
    params.append('department', department);
    params.append('yearOfStudy', yearOfStudy);

    try {
        const response = await fetch(API_BASE, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Action': 'add'
            },
            body: params.toString()
        });

        const result = await response.json();

        if (response.ok && result.success) {
            showToast('Student added successfully!', 'success');
            document.getElementById('addStudentForm').reset();
            // Reload list and switch to View Students
            await loadStudents();
            switchView('view-students');
        } else {
            showToast(result.message || 'Failed to add student.', 'error');
        }
    } catch (error) {
        console.error('Error adding student:', error);
        showToast('Error communicating with Java backend.', 'error');
    }
}

/**
 * Opens the Edit Student modal with pre-populated values
 */
function openEditModal(id) {
    const student = studentsList.find(s => s.id === id);
    if (!student) {
        showToast('Student details could not be found.', 'error');
        return;
    }

    document.getElementById('editStudentId').value = student.id;
    document.getElementById('editName').value = student.name;
    document.getElementById('editRegNo').value = student.regNo;
    document.getElementById('editEmail').value = student.email;
    document.getElementById('editPhone').value = student.phone;
    document.getElementById('editDepartment').value = student.department;
    document.getElementById('editYearOfStudy').value = student.yearOfStudy;

    const modal = document.getElementById('editStudentModal');
    if (modal) {
        modal.classList.add('active');
    }
}

/**
 * Closes the Edit Student modal
 */
function closeEditModal() {
    const modal = document.getElementById('editStudentModal');
    if (modal) {
        modal.classList.remove('active');
    }
}

/**
 * UPDATE OPERATION: Handles Edit Student form submission via POST
 */
async function handleUpdateStudent(e) {
    e.preventDefault();

    const id = document.getElementById('editStudentId').value;
    const name = document.getElementById('editName').value.trim();
    const regNo = document.getElementById('editRegNo').value.trim();
    const email = document.getElementById('editEmail').value.trim();
    const phone = document.getElementById('editPhone').value.trim();
    const department = document.getElementById('editDepartment').value;
    const yearOfStudy = document.getElementById('editYearOfStudy').value;

    if (!id || !name || !regNo || !email || !phone || !department || !yearOfStudy) {
        showToast('Please fill out all required fields!', 'error');
        return;
    }

    const params = new URLSearchParams();
    params.append('action', 'update');
    params.append('id', id);
    params.append('name', name);
    params.append('regNo', regNo);
    params.append('email', email);
    params.append('phone', phone);
    params.append('department', department);
    params.append('yearOfStudy', yearOfStudy);

    try {
        const response = await fetch(API_BASE, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Action': 'update'
            },
            body: params.toString()
        });

        const result = await response.json();

        if (response.ok && result.success) {
            showToast('Student updated successfully!', 'success');
            closeEditModal();
            await loadStudents();
        } else {
            showToast(result.message || 'Failed to update student.', 'error');
        }
    } catch (error) {
        console.error('Error updating student:', error);
        showToast('Error communicating with Java backend.', 'error');
    }
}

/**
 * Opens Delete Confirmation Modal
 */
function openDeleteModal(id, name, regNo) {
    deleteTargetId = id;
    const desc = document.getElementById('deleteModalDesc');
    if (desc) {
        desc.innerHTML = `Are you sure you want to delete student <strong>${escapeHtml(name)}</strong> (Reg No: <strong>${escapeHtml(regNo)}</strong>)? This action cannot be undone.`;
    }

    const modal = document.getElementById('deleteConfirmModal');
    if (modal) {
        modal.classList.add('active');
    }
}

/**
 * Closes Delete Confirmation Modal
 */
function closeDeleteModal() {
    deleteTargetId = null;
    const modal = document.getElementById('deleteConfirmModal');
    if (modal) {
        modal.classList.remove('active');
    }
}

/**
 * DELETE OPERATION: Executes student deletion via POST
 */
async function executeDeleteStudent() {
    if (!deleteTargetId) return;

    const params = new URLSearchParams();
    params.append('action', 'delete');
    params.append('id', deleteTargetId);

    try {
        const response = await fetch(API_BASE, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-Action': 'delete'
            },
            body: params.toString()
        });

        const result = await response.json();

        if (response.ok && result.success) {
            showToast('Student deleted successfully!', 'success');
            closeDeleteModal();
            await loadStudents();
        } else {
            showToast(result.message || 'Failed to delete student.', 'error');
        }
    } catch (error) {
        console.error('Error deleting student:', error);
        showToast('Error communicating with Java backend.', 'error');
    }
}

/**
 * SEARCH OPERATION: Handles dedicated search page query
 */
async function handleDedicatedSearch(e) {
    if (e) e.preventDefault();

    const query = document.getElementById('dedicatedSearchQuery').value.trim();
    const dept = document.getElementById('dedicatedSearchDept').value;
    const year = document.getElementById('dedicatedSearchYear').value;

    const url = `${API_BASE}?action=search&query=${encodeURIComponent(query)}&department=${encodeURIComponent(dept)}&year=${encodeURIComponent(year)}`;

    try {
        const response = await fetch(url);
        if (!response.ok) throw new Error('Search request failed');
        const results = await response.json();

        const countBadge = document.getElementById('searchResultCount');
        if (countBadge) {
            countBadge.textContent = `${results.length} record(s) found`;
        }

        renderStudentsTable(results, 'dedicatedSearchResultsBody');
    } catch (error) {
        console.error('Search error:', error);
        showToast('Error executing search.', 'error');
    }
}

/**
 * Resets dedicated search form and results
 */
function resetDedicatedSearch() {
    document.getElementById('dedicatedSearchForm').reset();
    handleDedicatedSearch();
}

/**
 * Toast Notification Popup
 */
function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;

    let icon = 'ℹ️';
    if (type === 'success') icon = '✅';
    if (type === 'error') icon = '⚠️';

    toast.innerHTML = `
        <span class="toast-icon">${icon}</span>
        <span class="toast-msg">${escapeHtml(message)}</span>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100%)';
        setTimeout(() => toast.remove(), 300);
    }, 3500);
}

// --- UTILITY FUNCTIONS ---

function validateEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function validatePhone(phone) {
    return /^[\d\s\+\-\(\)]{10,15}$/.test(phone);
}

function getInitials(name) {
    if (!name) return 'ST';
    const parts = name.trim().split(' ');
    if (parts.length === 1) return parts[0].substring(0, 2).toUpperCase();
    return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

function getDeptBadgeColor(dept) {
    if (!dept) return 'badge-blue';
    const lower = dept.toLowerCase();
    if (lower.includes('computer')) return 'badge-blue';
    if (lower.includes('information')) return 'badge-purple';
    if (lower.includes('electronic')) return 'badge-cyan';
    if (lower.includes('electrical')) return 'badge-amber';
    if (lower.includes('mechanical')) return 'badge-pink';
    if (lower.includes('civil')) return 'badge-emerald';
    return 'badge-blue';
}

function getDeptFillClass(dept) {
    if (!dept) return 'fill-cse';
    const lower = dept.toLowerCase();
    if (lower.includes('computer')) return 'fill-cse';
    if (lower.includes('information')) return 'fill-it';
    if (lower.includes('electronic')) return 'fill-ece';
    if (lower.includes('electrical')) return 'fill-eee';
    if (lower.includes('mechanical')) return 'fill-mech';
    if (lower.includes('civil')) return 'fill-civil';
    return 'fill-cse';
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#39;');
}

function escapeQuote(str) {
    if (!str) return '';
    return String(str).replace(/'/g, "\\'");
}
