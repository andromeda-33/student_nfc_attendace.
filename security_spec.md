# TapAttend NFC — Firestore Security Specification (Phase 0)

## 1. Data Invariants
1. **Authenticated & Verified Writes**: Every database write (`create`, `update`, `delete`) requires `request.auth != null` and `request.auth.token.email_verified == true`.
2. **Ownership & Attribution Integrity**:
   - A `Student` card mapping must record `createdByUid == request.auth.uid` on creation and cannot be reassigned to another creator. Only the creator (or admin) may update or delete a student record, while any verified instructor in the same university roster can read/sync it via `universityId`.
   - A `CustomCourse` must record `createdByUid == request.auth.uid` on creation.
   - An `AttendanceSession` is owned by `ownerUid == request.auth.uid`. Only the session owner can read, update, or delete the session or its attendance records.
   - An `AttendanceRecord` under `/sessions/{sessionId}/records/{recordId}` cannot exist without a valid parent `/sessions/{sessionId}` owned by `request.auth.uid`.
3. **Zero PII Storage**: Student documents contain only academic enrollment attributes (`regNumber`, `fullName`, `universityId`, `universityName`, `department`, `courseCode`, `courseName`, `cardUid`)—no personal emails, phone numbers, or home addresses.
4. **Temporal & Schema Rigidity**: `createdAt` and `updatedAt` must equal `request.time`. All strings and arrays are strictly bounded, and shadow/ghost fields are blocked via `hasAll` + `hasOnly`.

## 2. "Dirty Dozen" Adversarial Payloads
1. Unauthenticated `create` on `/students/stu_1`.
2. Verified user `create` on `/students/stu_2` spoofing `createdByUid: "other_uid"`.
3. Shadow field injection (`isAdmin: true`) on `/students/stu_3`.
4. Oversized `cardUid` (> 32 chars) or invalid lowercase/symbols in `cardUid`.
5. Oversized `id` path variable (`1.5KB` string) on `/students/{studentId}`.
6. Spoofed `createdAt` timestamp (client clock instead of `request.time`).
7. Non-owner attempt to update or delete another instructor's `/sessions/{sessionId}`.
8. Orphaned `/sessions/{nonExistent}/records/{recId}` write.
9. Unbounded `allowedCourses` array (> 15 elements) on `/sessions/{sessionId}`.
10. Invalid `sessionType` enum value (e.g. `"HACKED"`) on `/sessions/{sessionId}`.
11. Mutating immutable `ownerUid` or `createdAt` during session update.
12. Querying `/sessions` without `where('ownerUid', '==', auth.uid)` (rule-level list enforcement).
