const truffleAssert = require('truffle-assertions');
const CertificateRepository = artifacts.require('./CertificateRegistry.sol')

contract("CertificateRepository", async accounts => {

    it("should add certificate if trusted issuer", async () => {
        const checksum = "checksum1"

        const certificateRepositoryInstance = await CertificateRepository.deployed();
        await truffleAssert.passes(
            certificateRepositoryInstance.addCertificate(checksum, "jan", "kowalski", 1)
        );

        await certificateRepositoryInstance.invalidate(checksum)
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

        await certificateRepositoryInstance.invalidate(checksum)
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

        await certificateRepositoryInstance.invalidate(checksum)
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

        const array = await certificateRepositoryInstance.getChecksums()

        assert.equal(
              array.length,
              0,
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

        await certificateRepositoryInstance.invalidate(checksum)
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

    it("should return all checksums", async () => {

        const checksum = "checksum11"
        const checksum2 = "checksum12"
        const checksum3 = "checksum13"

        const certificateRepositoryInstance = await CertificateRepository.deployed();
        await certificateRepositoryInstance.addTrustedIssuer(accounts[1])

        await certificateRepositoryInstance.addCertificate(checksum, "jan", "kowalski", 1)
        await certificateRepositoryInstance.addCertificate(checksum2, "jan", "nowak", 1, {from: accounts[1]})

        await certificateRepositoryInstance.addCertificate(checksum3, "jan", "wójcik", 1)
        await certificateRepositoryInstance.invalidate(checksum3)

        const array = await certificateRepositoryInstance.getChecksums()

        assert.equal(
              array[0],
              'checksum11',
              "Wrong checksums returned"
            );

        assert.equal(
              array[1],
              'checksum12',
              "Wrong checksums returned"
            );

        assert.equal(
              array.length,
              2,
              "Wrong checksums returned"
            );

        await certificateRepositoryInstance.invalidate(checksum)
        await certificateRepositoryInstance.invalidate(checksum2, {from: accounts[1]})

    });

    it("should get all certificates issued by issuer", async () => {
        const checksum = "checksum13"
        const checksum2 = "checksum14"

        const certificateRepositoryInstance = await CertificateRepository.deployed();

        await certificateRepositoryInstance.addTrustedIssuer(accounts[1])

        await certificateRepositoryInstance.addCertificate(checksum, "jan", "kowalski", 1)
        await certificateRepositoryInstance.addCertificate(checksum2, "jan", "nowak", 1, {from: accounts[1]})


        const array = await certificateRepositoryInstance.getCertificatesIssuedBy(accounts[0])

        assert.equal(
              array.length,
              1,
              "Wrong certificates returned"
            );

        assert.equal(
              array[0].checksum,
              checksum,
              "Wrong certificates returned"
            );

        await certificateRepositoryInstance.invalidate(checksum2, {from: accounts[1]})
        await certificateRepositoryInstance.invalidate(checksum)
    });

        it("should get all certificates", async () => {
            const checksum = "checksum15"
            const checksum2 = "checksum16"

            const certificateRepositoryInstance = await CertificateRepository.deployed();

            await certificateRepositoryInstance.addTrustedIssuer(accounts[1])

            await certificateRepositoryInstance.addCertificate(checksum, "jan", "kowalski", 1)
            await certificateRepositoryInstance.addCertificate(checksum2, "jan", "nowak", 1, {from: accounts[1]})


            const array = await certificateRepositoryInstance.getAllCertificates()

            assert.equal(
                  array.length,
                  2,
                  "Wrong certificates returned"
                );

            assert.equal(
                  array[0].checksum,
                  checksum,
                  "Wrong certificates returned"
                );

            assert.equal(
                  array[1].checksum,
                  checksum2,
                  "Wrong certificates returned"
                );

            await certificateRepositoryInstance.invalidate(checksum)
            await certificateRepositoryInstance.invalidate(checksum2, {from: accounts[1]})
        });

});