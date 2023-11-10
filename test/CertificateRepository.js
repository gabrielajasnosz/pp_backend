const truffleAssert = require('truffle-assertions');
const CertificateRepository = artifacts.require('./CertificateRegistry.sol')

contract("CertificateRepository", async accounts => {

    it("should add certificate if trusted issuer", async () => {
        const checksum = "checksum1"

        const certificateRepositoryInstance = await CertificateRepository.deployed();
        await truffleAssert.passes(
            certificateRepositoryInstance.addCertificate(checksum, "jan", "kowalski", 1)
        );
    });

    it("should fail to add certificate if not trusted issuer", async () => {
        const checksum = "checksum2"

        const certificateRepositoryInstance = await CertificateRepository.deployed();
        await truffleAssert.fails(
            certificateRepositoryInstance.addCertificate(checksum, "jan", "kowalski", 1, {from: accounts[1]}),
            truffleAssert.ErrorType.REVERT,
            "You are not a trusted issuer!"
        );
    });

    it("should fail to add certificate add certificate if already present", async () => {
        const checksum = "checksum3"

        const certificateRepositoryInstance = await CertificateRepository.deployed();
        certificateRepositoryInstance.addCertificate(checksum, "jan", "kowalski", 1)
        await truffleAssert.fails(
            certificateRepositoryInstance.addCertificate(checksum, "jan", "kowalski", 1),
            truffleAssert.ErrorType.REVERT,
            "Certificate already present!"
        );
    });

    it("should fail to add certificate if checksum empty", async () => {
        const checksum = ""

        const certificateRepositoryInstance = await CertificateRepository.deployed();
        await truffleAssert.fails(
            certificateRepositoryInstance.addCertificate(checksum, "jan", "kowalski", 1),
            truffleAssert.ErrorType.REVERT,
            "File checksum must not be empty!"
        );
    });

    it("should fail to add certificate if _recipient_name empty", async () => {
        const checksum = "checksum4"

        const certificateRepositoryInstance = await CertificateRepository.deployed();
        await truffleAssert.fails(
            certificateRepositoryInstance.addCertificate(checksum, "", "kowalski", 1),
            truffleAssert.ErrorType.REVERT,
            "Recipient name must not be empty!"
        );
    });

    it("should fail to add certificate if _recipient_surname empty", async () => {
        const checksum = "checksum5"

        const certificateRepositoryInstance = await CertificateRepository.deployed();
        await truffleAssert.fails(
            certificateRepositoryInstance.addCertificate(checksum, "jan", "", 1),
            truffleAssert.ErrorType.REVERT,
            "Recipient surname must not be empty!"
        );
    });

    it("should fail to add certificate if _days_valid == 0", async () => {
        const checksum = "checksum6"

        const certificateRepositoryInstance = await CertificateRepository.deployed();
        await truffleAssert.fails(
            certificateRepositoryInstance.addCertificate(checksum, "jan", "kowalski", 0),
            truffleAssert.ErrorType.REVERT,
            "Contract must be valid for at least 1 day!"
        );
    });

    it("should add certificate correctly", async () => {
        const checksum = "checksum7"
        const _days_valid = 1

        const certificateRepositoryInstance = await CertificateRepository.deployed();
        await certificateRepositoryInstance.addCertificate(checksum, "jan", "kowalski", _days_valid)

        const certificate = await certificateRepositoryInstance.getCertificate(checksum)
        assert.equal(
              certificate.recipient.name,
              "jan",
              "Name is not correct"
            );
        assert.equal(
              certificate.recipient.surname,
              "kowalski",
              "Surname is not correct"
            );
        assert.equal(
              parseInt(certificate.expireDate),
              parseInt(certificate.issueDate) + (_days_valid * 86400),
              "Expire date is not correct"
            );
        assert.equal(
              certificate.issuer,
              accounts[0],
              "Issuer is not correct"
            );
        assert.equal(
              certificate.checksum,
              checksum,
              "Checksum is not correct"
            );
    });

    it("should add and remove trusted issuer", async () => {
        const certificateRepositoryInstance = await CertificateRepository.deployed();

        const isTrustedIssuerBefore = await certificateRepositoryInstance.isTrustedIssuer(accounts[1])
        assert.equal(isTrustedIssuerBefore,false);

        await certificateRepositoryInstance.addTrustedIssuer(accounts[1])

        const isTrustedIssuerAfter = await certificateRepositoryInstance.isTrustedIssuer(accounts[1])
        assert.equal(isTrustedIssuerAfter,true);

        await certificateRepositoryInstance.removeTrustedIssuer(accounts[1])

        const isTrustedIssuerAfterRemoval = await certificateRepositoryInstance.isTrustedIssuer(accounts[1])
        assert.equal(isTrustedIssuerAfterRemoval,false);
    });


    it("should invalidate certificate correctly", async () => {
        const checksum = "checksum8"
        const _days_valid = 1

        const certificateRepositoryInstance = await CertificateRepository.deployed();
        await certificateRepositoryInstance.addCertificate(checksum, "jan", "kowalski", _days_valid)

        const certificate = await certificateRepositoryInstance.getCertificate(checksum)
        assert.equal(
              certificate.checksum,
              checksum,
              "Contract was not added"
            );

        await certificateRepositoryInstance.invalidate(checksum)

        const invalidCertificate = await certificateRepositoryInstance.getCertificate(checksum)

        assert.equal(
              invalidCertificate.checksum,
              "",
              "Contract was not invalidated"
            );
    });

    it("should fail to invalidate certificate if not certificate's issuer", async () => {
        const checksum = "checksum9"

        const certificateRepositoryInstance = await CertificateRepository.deployed();

        await certificateRepositoryInstance.addCertificate(checksum, "jan", "kowalski", 1)

        await truffleAssert.fails(
            certificateRepositoryInstance.invalidate(checksum, {from: accounts[1]}),
            truffleAssert.ErrorType.REVERT,
            "You must be contract's issuer!"
        );
    });

    it("should fail to invalidate certificate if certificate does not exist", async () => {
        const checksum = "checksum10"

        const certificateRepositoryInstance = await CertificateRepository.deployed();

        await truffleAssert.fails(
            certificateRepositoryInstance.invalidate(checksum),
            truffleAssert.ErrorType.REVERT,
            "Certificate does not exist!"
        );
    });

});