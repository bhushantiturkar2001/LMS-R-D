# Platform Admin Dashboard

<div align="center">
  <h1 style="color:#4F46E5;">🛡️ Platform Admin — Roles, Responsibilities & Dashboard</h1>
  <p><strong>Project:</strong> Unified LMS &nbsp;|&nbsp; <strong>Researcher:</strong> Bhushan &nbsp;|&nbsp; <strong>Date:</strong> May 12, 2026</p>
</div>

---

## 📋 Table of Contents

<table>
<tr><td>1.</td><td><a href="#who-is-platform-admin">Who is Platform Admin</a></td></tr>
<tr><td>2.</td><td><a href="#hierarchy">Platform Admin Hierarchy</a></td></tr>
<tr><td>3.</td><td><a href="#responsibilities">Core Responsibilities</a></td></tr>
<tr><td>4.</td><td><a href="#institute-management">Institute Management Flow</a></td></tr>
<tr><td>5.</td><td><a href="#dashboard-widgets">Dashboard Widgets & Features</a></td></tr>
<tr><td>6.</td><td><a href="#super-admin-features">Platform Super Admin Features</a></td></tr>
<tr><td>7.</td><td><a href="#sub-admin-features">Platform Sub Admin Features</a></td></tr>
<tr><td>8.</td><td><a href="#api-overview">API Overview</a></td></tr>
</table>

---

<h2 id="who-is-platform-admin">👤 Who is Platform Admin</h2>

<table>
<thead>
<tr>
  <th>Role</th>
  <th>Real World Example</th>
  <th>Access Level</th>
</tr>
</thead>
<tbody>
<tr>
  <td>🔴 <strong>Platform Super Admin</strong></td>
  <td>Your company's CEO / CTO / Founder</td>
  <td>Full platform control — everything</td>
</tr>
<tr>
  <td>🟡 <strong>Platform Sub Admin</strong></td>
  <td>Operations team / Support manager</td>
  <td>Limited — assigned modules only</td>
</tr>
</tbody>
</table>

> **Simple words mein:** Platform Admin = Jo poora LMS platform chalata hai. Institutes, instructors, students — sab inke under hain. Yeh god-mode user hai.

---

<h2 id="hierarchy">🏗️ Platform Admin Hierarchy</h2>

```
Platform Super Admin  ← TOP LEVEL (God Mode)
        │
        ├── Manages → All Institutes
        │               └── Institute Super Admin
        │                       └── Branch Admin
        │                               └── Tutors
        │
        ├── Manages → All Private Instructors
        │
        ├── Manages → All Students
        │
        ├── Manages → Platform Settings
        │
        └── Platform Sub Admin  ← Delegated access
                ├── Can manage: Institutes (if given access)
                ├── Can manage: Support tickets
                ├── Can manage: Reports
                └── Cannot: Financial settings, platform config
```

---

<h2 id="responsibilities">✅ Core Responsibilities</h2>

<table>
<thead>
<tr>
  <th>#</th>
  <th>Responsibility</th>
  <th>Description</th>
  <th>Who Does It</th>
</tr>
</thead>
<tbody>
<tr>
  <td>1</td>
  <td>🏫 <strong>Institute Onboarding</strong></td>
  <td>Naya institute add karo, approve karo, activate karo</td>
  <td>Super Admin</td>
</tr>
<tr>
  <td>2</td>
  <td>👨‍🏫 <strong>Instructor Approval</strong></td>
  <td>Private instructor ka application review karo, approve/reject karo</td>
  <td>Super Admin / Sub Admin</td>
</tr>
<tr>
  <td>3</td>
  <td>📦 <strong>Plan & Subscription Management</strong></td>
  <td>Institute ke liye plans set karo (Basic, Pro, Enterprise)</td>
  <td>Super Admin</td>
</tr>
<tr>
  <td>4</td>
  <td>💰 <strong>Revenue & Finance Oversight</strong></td>
  <td>Platform ka total revenue, payouts, commissions dekho</td>
  <td>Super Admin</td>
</tr>
<tr>
  <td>5</td>
  <td>🎫 <strong>Support Ticket Management</strong></td>
  <td>Institutes/students ke issues resolve karo</td>
  <td>Sub Admin</td>
</tr>
<tr>
  <td>6</td>
  <td>📊 <strong>Platform Analytics</strong></td>
  <td>Total users, active sessions, revenue trends dekho</td>
  <td>Super Admin</td>
</tr>
<tr>
  <td>7</td>
  <td>🔧 <strong>Platform Configuration</strong></td>
  <td>Email settings, payment gateway, SMS provider configure karo</td>
  <td>Super Admin</td>
</tr>
<tr>
  <td>8</td>
  <td>🚨 <strong>Content Moderation</strong></td>
  <td>Reported courses/content review karo, remove karo</td>
  <td>Super Admin / Sub Admin</td>
</tr>
<tr>
  <td>9</td>
  <td>📢 <strong>Announcements</strong></td>
  <td>Platform-wide announcements push karo (maintenance, new features)</td>
  <td>Super Admin</td>
</tr>
<tr>
  <td>10</td>
  <td>🔐 <strong>Role & Permission Management</strong></td>
  <td>Sub admins create karo, unhe specific modules assign karo</td>
  <td>Super Admin</td>
</tr>
<tr>
  <td>11</td>
  <td>📋 <strong>Audit Logs</strong></td>
  <td>Kaun kya kar raha hai platform pe — full activity log</td>
  <td>Super Admin</td>
</tr>
<tr>
  <td>12</td>
  <td>🤖 <strong>AI Features Control</strong></td>
  <td>AI course generation, recommendation engine on/off karo</td>
  <td>Super Admin</td>
</tr>
</tbody>
</table>

---

<h2 id="institute-management">🏫 Institute Management Flow</h2>

> This is the **most important** responsibility of Platform Admin

```
Step 1: Institute applies / Platform Admin adds institute manually
        │
        ▼
Step 2: Platform Admin reviews institute details
        ├── Institute name, address, type
        ├── Contact person details
        ├── Documents (if required)
        └── Plan selection (Basic / Pro / Enterprise)
        │
        ▼
Step 3: Platform Admin APPROVES institute
        │
        ▼
Step 4: System auto-creates:
        ├── Institute Super Admin account (credentials sent via email)
        ├── Institute workspace (subdomain or ID)
        └── Default settings applied
        │
        ▼
Step 5: Institute Super Admin logs in
        ├── Sets up branches
        ├── Adds Branch Admins
        ├── Adds Tutors
        └── Starts adding students/courses
        │
        ▼
Step 6: Platform Admin monitors
        ├── Institute activity
        ├── Revenue generated
        ├── Student count
        └── Support tickets
```

<table>
<thead>
<tr>
  <th>Institute Status</th>
  <th>Meaning</th>
  <th>Platform Admin Action</th>
</tr>
</thead>
<tbody>
<tr><td>🟡 Pending</td><td>Application submitted, not reviewed</td><td>Review karo</td></tr>
<tr><td>🟢 Active</td><td>Approved, fully operational</td><td>Monitor karo</td></tr>
<tr><td>🔴 Suspended</td><td>Temporarily blocked (payment due / violation)</td><td>Suspend/Unsuspend</td></tr>
<tr><td>⚫ Deactivated</td><td>Permanently closed</td><td>Archive karo</td></tr>
</tbody>
</table>

---

<h2 id="dashboard-widgets">📊 Dashboard Widgets & Features</h2>

### 🔢 Top Stats Cards (Always Visible)

<table>
<thead>
<tr>
  <th>Widget</th>
  <th>Shows</th>
  <th>Why Important</th>
</tr>
</thead>
<tbody>
<tr>
  <td>👥 Total Users</td>
  <td>Students + Instructors + Institute users</td>
  <td>Platform growth track karo</td>
</tr>
<tr>
  <td>🏫 Total Institutes</td>
  <td>Active / Pending / Suspended count</td>
  <td>Institute health dekho</td>
</tr>
<tr>
  <td>💰 Total Revenue</td>
  <td>This month vs last month</td>
  <td>Business performance</td>
</tr>
<tr>
  <td>📹 Live Sessions Today</td>
  <td>Currently active live classes</td>
  <td>Platform load monitor karo</td>
</tr>
<tr>
  <td>🎫 Open Tickets</td>
  <td>Unresolved support tickets</td>
  <td>Support backlog dekho</td>
</tr>
<tr>
  <td>📈 New Enrollments</td>
  <td>Today / This week</td>
  <td>Growth trend</td>
</tr>
</tbody>
</table>

---

### 📋 Main Dashboard Sections

<table>
<thead>
<tr>
  <th>Section</th>
  <th>Features Inside</th>
</tr>
</thead>
<tbody>
<tr>
  <td>🏫 <strong>Institute Management</strong></td>
  <td>
    List of all institutes (search, filter by status)<br/>
    Add new institute manually<br/>
    Approve / Reject pending institutes<br/>
    View institute details (branches, students, revenue)<br/>
    Suspend / Activate institute<br/>
    Assign / Change subscription plan<br/>
    Login as Institute Admin (impersonate)
  </td>
</tr>
<tr>
  <td>👨‍🏫 <strong>Instructor Management</strong></td>
  <td>
    All private instructors list<br/>
    Pending approval requests<br/>
    Approve / Reject instructor applications<br/>
    View instructor courses and earnings<br/>
    Suspend / Ban instructor<br/>
    Payout history
  </td>
</tr>
<tr>
  <td>👨‍🎓 <strong>Student Management</strong></td>
  <td>
    All students list (platform-wide)<br/>
    Search by name, email, institute<br/>
    View student profile and enrollment history<br/>
    Block / Unblock student<br/>
    Refund management
  </td>
</tr>
<tr>
  <td>💰 <strong>Finance & Revenue</strong></td>
  <td>
    Total platform revenue (monthly/yearly chart)<br/>
    Revenue by institute<br/>
    Revenue by instructor<br/>
    Commission settings (platform cut %)<br/>
    Payout management (instructor payouts)<br/>
    Invoice generation<br/>
    Refund requests
  </td>
</tr>
<tr>
  <td>📦 <strong>Plans & Subscriptions</strong></td>
  <td>
    Create / Edit subscription plans (Basic, Pro, Enterprise)<br/>
    Features per plan (max students, branches, storage)<br/>
    Assign plan to institute<br/>
    Plan expiry alerts<br/>
    Upgrade / Downgrade institute plan
  </td>
</tr>
<tr>
  <td>📹 <strong>Live Session Monitoring</strong></td>
  <td>
    Currently active live sessions (all institutes)<br/>
    Session details (room name, participants, duration)<br/>
    Force end a session (emergency)<br/>
    Recording status<br/>
    Server load (LiveKit SFU health)
  </td>
</tr>
<tr>
  <td>🎫 <strong>Support & Tickets</strong></td>
  <td>
    All support tickets (open / in-progress / resolved)<br/>
    Assign ticket to sub admin<br/>
    Reply to tickets<br/>
    Ticket categories (payment, technical, account)<br/>
    SLA tracking (response time)
  </td>
</tr>
<tr>
  <td>📊 <strong>Analytics & Reports</strong></td>
  <td>
    User growth chart (monthly)<br/>
    Revenue trend chart<br/>
    Top performing institutes<br/>
    Top performing instructors<br/>
    Most enrolled courses<br/>
    Live session statistics<br/>
    Export reports (CSV / PDF)
  </td>
</tr>
<tr>
  <td>📢 <strong>Announcements</strong></td>
  <td>
    Create platform-wide announcement<br/>
    Target: All users / Institutes only / Students only<br/>
    Schedule announcement<br/>
    Push notification / Email / In-app banner<br/>
    Announcement history
  </td>
</tr>
<tr>
  <td>🔧 <strong>Platform Settings</strong></td>
  <td>
    Email provider config (SMTP / SendGrid)<br/>
    SMS provider config (Twilio / MSG91)<br/>
    Payment gateway config (Razorpay / Stripe)<br/>
    LiveKit server config<br/>
    Storage config (S3 / local)<br/>
    Platform name, logo, theme<br/>
    Maintenance mode on/off
  </td>
</tr>
<tr>
  <td>🔐 <strong>Admin Management</strong></td>
  <td>
    Create Sub Admins<br/>
    Assign modules to Sub Admin<br/>
    View Sub Admin activity<br/>
    Revoke Sub Admin access
  </td>
</tr>
<tr>
  <td>📋 <strong>Audit Logs</strong></td>
  <td>
    All admin actions log (who did what, when)<br/>
    Filter by admin, action type, date<br/>
    Export audit logs<br/>
    Security alerts (suspicious activity)
  </td>
</tr>
<tr>
  <td>🤖 <strong>AI Controls</strong></td>
  <td>
    AI Course Generation on/off<br/>
    Recommendation Engine on/off<br/>
    AI Tutor on/off<br/>
    LLM provider settings (OpenAI / custom)<br/>
    AI usage stats and cost
  </td>
</tr>
</tbody>
</table>

---

<h2 id="super-admin-features">🔴 Platform Super Admin — Full Feature List</h2>

<table>
<thead>
<tr><th>Module</th><th>Can Do</th></tr>
</thead>
<tbody>
<tr><td>Institute</td><td>Add, Edit, Approve, Suspend, Delete, Impersonate</td></tr>
<tr><td>Instructor</td><td>Approve, Suspend, Ban, View earnings, Manage payouts</td></tr>
<tr><td>Student</td><td>View, Block, Refund, Delete account</td></tr>
<tr><td>Finance</td><td>Full revenue access, Commission settings, Payouts</td></tr>
<tr><td>Plans</td><td>Create, Edit, Delete plans, Assign to institutes</td></tr>
<tr><td>Live Sessions</td><td>Monitor all, Force end, View recordings</td></tr>
<tr><td>Support</td><td>View all tickets, Assign, Reply, Close</td></tr>
<tr><td>Analytics</td><td>Full platform analytics, Export all reports</td></tr>
<tr><td>Announcements</td><td>Create, Schedule, Target, Delete</td></tr>
<tr><td>Settings</td><td>Full platform configuration</td></tr>
<tr><td>Sub Admins</td><td>Create, Assign permissions, Revoke</td></tr>
<tr><td>Audit Logs</td><td>View all logs, Export</td></tr>
<tr><td>AI Controls</td><td>Full AI feature management</td></tr>
</tbody>
</table>

---

<h2 id="sub-admin-features">🟡 Platform Sub Admin — Limited Feature List</h2>

> Sub Admin ko sirf wahi modules dikhenge jo Super Admin ne assign kiye hain.

<table>
<thead>
<tr><th>Module</th><th>Typical Access</th><th>Cannot Do</th></tr>
</thead>
<tbody>
<tr><td>Institute</td><td>View list, View details</td><td>Approve, Delete, Impersonate</td></tr>
<tr><td>Instructor</td><td>View applications, Basic info</td><td>Approve/Reject, Payouts</td></tr>
<tr><td>Student</td><td>View profile, Basic support</td><td>Delete, Refund</td></tr>
<tr><td>Finance</td><td>❌ No access by default</td><td>All financial data</td></tr>
<tr><td>Support</td><td>View tickets, Reply, Close</td><td>Assign to others</td></tr>
<tr><td>Analytics</td><td>Basic reports only</td><td>Export, Full revenue data</td></tr>
<tr><td>Announcements</td><td>View only</td><td>Create, Send</td></tr>
<tr><td>Settings</td><td>❌ No access</td><td>All settings</td></tr>
<tr><td>Audit Logs</td><td>❌ No access</td><td>All logs</td></tr>
</tbody>
</table>

---

<h2 id="api-overview">🔌 API Overview — Platform Admin Service</h2>

<table>
<thead>
<tr>
  <th>Method</th>
  <th>Endpoint</th>
  <th>Description</th>
  <th>Who Can Call</th>
</tr>
</thead>
<tbody>
<tr><td>POST</td><td>/api/admin/institutes</td><td>Add new institute</td><td>Super Admin</td></tr>
<tr><td>GET</td><td>/api/admin/institutes</td><td>List all institutes</td><td>Super + Sub Admin</td></tr>
<tr><td>PATCH</td><td>/api/admin/institutes/{id}/approve</td><td>Approve institute</td><td>Super Admin</td></tr>
<tr><td>PATCH</td><td>/api/admin/institutes/{id}/suspend</td><td>Suspend institute</td><td>Super Admin</td></tr>
<tr><td>GET</td><td>/api/admin/instructors/pending</td><td>Pending instructor approvals</td><td>Super Admin</td></tr>
<tr><td>PATCH</td><td>/api/admin/instructors/{id}/approve</td><td>Approve instructor</td><td>Super Admin</td></tr>
<tr><td>GET</td><td>/api/admin/analytics/revenue</td><td>Revenue analytics</td><td>Super Admin</td></tr>
<tr><td>GET</td><td>/api/admin/analytics/users</td><td>User growth analytics</td><td>Super Admin</td></tr>
<tr><td>GET</td><td>/api/admin/live-sessions</td><td>All active live sessions</td><td>Super Admin</td></tr>
<tr><td>POST</td><td>/api/admin/announcements</td><td>Create announcement</td><td>Super Admin</td></tr>
<tr><td>GET</td><td>/api/admin/tickets</td><td>All support tickets</td><td>Super + Sub Admin</td></tr>
<tr><td>POST</td><td>/api/admin/sub-admins</td><td>Create sub admin</td><td>Super Admin</td></tr>
<tr><td>GET</td><td>/api/admin/audit-logs</td><td>Platform audit logs</td><td>Super Admin</td></tr>
</tbody>
</table>

---

<div align="center">
  <p><em>Document created: May 12, 2026 &nbsp;|&nbsp; Researcher: Bhushan &nbsp;|&nbsp; Unified LMS Platform</em></p>
</div>
