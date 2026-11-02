const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const ADMIN_UID = "admin_789";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      host: emulatorHost,
      port: emulatorPort,
      rules: rules,
    },
  });
});

beforeEach(async () => {
  await testEnv.clearFirestore();
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

test("Unauthenticated user cannot read members", async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthedDb.collection("members").doc("alice_123").get());
});

test("Alice can create and read her own member record", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com" }).firestore();
  const aliceDoc = aliceDb.collection("members").doc(ALICE_UID);
  
  await assertSucceeds(aliceDoc.set({
    userId: ALICE_UID,
    fullName: "Alice Smith",
    memberNumber: "WF-2026-0001",
    email: "alice@example.com",
    phone: "555-0199",
    category: "Low Income Family",
    status: "Active",
    householdSize: 4,
    monthlyIncome: 1200.0,
    address: "123 Hope Ave",
    emergencyContact: "John Smith (555-0100)",
    notes: "Initial enrollment",
    createdAt: new Date(),
  }));

  const snapshot = await assertSucceeds(aliceDoc.get());
});

test("Bob cannot read Alice's member record", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com" }).firestore();
  await aliceDb.collection("members").doc(ALICE_UID).set({
    userId: ALICE_UID,
    fullName: "Alice Smith",
    memberNumber: "WF-2026-0001",
    category: "Low Income Family",
    status: "Active",
    createdAt: new Date(),
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@example.com" }).firestore();
  await assertFails(bobDb.collection("members").doc(ALICE_UID).get());
});

test("Admin (eangatia@gmail.com) can read Alice's member record", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com" }).firestore();
  await aliceDb.collection("members").doc(ALICE_UID).set({
    userId: ALICE_UID,
    fullName: "Alice Smith",
    memberNumber: "WF-2026-0001",
    category: "Low Income Family",
    status: "Active",
    createdAt: new Date(),
  });

  const adminDb = testEnv.authenticatedContext(ADMIN_UID, { email: "eangatia@gmail.com" }).firestore();
  await assertSucceeds(adminDb.collection("members").doc(ALICE_UID).get());
});

test("Alice can submit a case and Bob cannot modify it", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com" }).firestore();
  const caseDoc = aliceDb.collection("cases").doc("case_001");
  
  await assertSucceeds(caseDoc.set({
    memberId: ALICE_UID,
    userId: ALICE_UID,
    caseNumber: "CASE-1001",
    title: "Emergency Medical Relief",
    category: "Medical Assistance",
    status: "Submitted",
    priority: "Urgent",
    requestedAmount: 500.0,
    approvedAmount: 0.0,
    description: "Assistance with prescription medications",
    createdAt: new Date(),
  }));

  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@example.com" }).firestore();
  await assertFails(bobDb.collection("cases").doc("case_001").update({
    status: "Approved"
  }));
});
