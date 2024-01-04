// SPDX-License-Identifier: MIT
pragma solidity >=0.6.0 <0.9.0;

contract CertificateRegistry {

    address contractOwner;

    string[] checksums;

    struct Recipient {
        string name;
        string surname;
        string email;
    }

    struct Certificate {
        string checksum; // unique hash of certificate generated on frontend
        uint256 issueDate;
        uint256 expireDate;
        address issuer;
        string certName;
        Recipient recipient;
        string issuer_identification_name;
    }

    struct BulkCertificateData {
        string checksum;
        Recipient recipient;
        uint256 days_valid;
        string cert_name;
        string issuer_identification_name;
    }

    event SuccessfullyAddedCertificate(
        string indexed checksum,
        string recipient_name,
        string recipient_surname,
        string recipient_email,
        string issuer_identification_name
    );

    event FailedAddingCertificate(string indexed checksum, string reason);

    mapping(string => Certificate) certificates;

    mapping(address => bool) public trustedIssuers;
    mapping(address => bool) public admins;

    constructor() {
        contractOwner = msg.sender;
        //Wallet that is deploying contract to network is becoming owner as well as first trusted issuer
        trustedIssuers[msg.sender] = true;
        admins[msg.sender] = true;
    }

    //Validators
    modifier validateNotEmptyAddress(address _input) {
        require(_input != address(0), "Address cannot be empty");
        _;
    }

    modifier validateCerAddProperties(
        string memory _checksum,
        Recipient memory _recipient,
        uint256 _days_valid,
        string memory _cert_name,
        string memory _issuer_identification_name
    ) {
        bool valid;
        string memory reason;
        (valid, reason) = isCertificateValid(
            _checksum,
            _recipient,
            _days_valid,
            _cert_name,
            _issuer_identification_name
        );
        if(!valid) {
            revert(reason);
        }
        _;
    }

    modifier validateInvalidateCerProperties(string memory _checksum) {
        require(
            bytes(certificates[_checksum].checksum).length > 0,
            "Certificate does not exist!"
        );
        require(
            certificates[_checksum].issuer == msg.sender,
            "You must be contract's issuer!"
        );
        _;
    }

    modifier validateSenderIsNotOwnerOrAdmin(address _address) {
        require(
            _address != msg.sender && _address != contractOwner,
            "You cannot remove contract owner or yourself!"
        );
        _;
    }

    //Users management
    modifier onlyTrustedIssuer() {
        require(trustedIssuers[msg.sender], "You are not a trusted issuer!");
        _;
    }

    modifier onlyOwner() {
        require(contractOwner == msg.sender, "You are not an owner!");
        _;
    }

    modifier onlyAdmins() {
        require(admins[msg.sender], "You are not an admin!");
        _;
    }

    function addTrustedIssuer(address _issuer)
        public
        onlyAdmins
        validateNotEmptyAddress(_issuer)
    {
        trustedIssuers[_issuer] = true;
    }

    function addAdmin(address _admin)
        public
        onlyOwner
        validateNotEmptyAddress(_admin)
    {
        admins[_admin] = true;
    }

    function removeTrustedIssuer(address _issuer)
        public
        onlyAdmins
        validateNotEmptyAddress(_issuer)
        validateSenderIsNotOwnerOrAdmin(_issuer)
    {
        trustedIssuers[_issuer] = false;
    }

    function removeAdmin(address _admin)
        public
        onlyOwner
        validateNotEmptyAddress(_admin)
        validateSenderIsNotOwnerOrAdmin(_admin)
    {
        admins[_admin] = false;
    }

    function isTrustedIssuer(address _issuer) public view returns (bool) {
        return trustedIssuers[_issuer];
    }

    function isAdmin(address _admin) public view returns (bool) {
        return admins[_admin];
    }

    function isContractOwner(address _owner) public view returns (bool) {
        return contractOwner == _owner;
    }

    function addVerifiedCertificate(
        string memory _checksum,
        Recipient memory _recipient,
        uint256 _days_valid,
        string memory _cert_name,
        string memory _issuer_identification_name
    ) private {
        certificates[_checksum] = Certificate(
            _checksum,
            block.timestamp,
            block.timestamp + (_days_valid * 1 days),
            msg.sender,
            _cert_name,
            _recipient,
            _issuer_identification_name
        );
        checksums.push(_checksum);

        emit SuccessfullyAddedCertificate(
            _checksum,
            _recipient.name,
            _recipient.surname,
            _recipient.email,
            _issuer_identification_name
        );
    }

    // Certificate management
    function addCertificate(
        string memory _checksum,
        Recipient memory _recipient,
        uint256 _days_valid,
        string memory _cert_name,
        string memory _issuer_identification_name
    )
        public
        onlyTrustedIssuer
        validateCerAddProperties(
            _checksum,
            _recipient,
            _days_valid,
            _cert_name,
            _issuer_identification_name
        )
    {
        addVerifiedCertificate(
            _checksum,
            _recipient,
            _days_valid,
            _cert_name,
            _issuer_identification_name
        );
    }

    function getCertificate(string memory _checksum)
        public
        view
        returns (Certificate memory result)
    {
        result = certificates[_checksum];
    }

    function invalidate(string memory _checksum)
        public
        validateInvalidateCerProperties(_checksum)
    {
        uint256 index = indexOf(_checksum);

        checksums[index] = checksums[checksums.length - 1];
        checksums.pop();

        delete certificates[_checksum];
    }

    function getAllCertificates()
        public
        view
        returns (Certificate[] memory issued)
    {
        issued = new Certificate[](checksums.length);
        for (uint256 i = 0; i < checksums.length; i++) {
            issued[i] = certificates[checksums[i]];
        }
        return issued;
    }

    function getCertificatesIssuedBy(address _issuer)
        public
        view
        returns (Certificate[] memory issued)
    {
        uint256 found = 0;

        Certificate[] memory allCerts = new Certificate[](checksums.length);

        for (uint256 i = 0; i < checksums.length; i++) {
            Certificate memory cert = certificates[checksums[i]];
            if (cert.issuer == _issuer) {
                allCerts[found] = cert;
                found++;
            }
        }

        issued = new Certificate[](found);

        for (uint256 i = 0; i < found; i++) {
            issued[i] = allCerts[i];
        }

        return issued;
    }

    function getChecksums() public view returns (string[] memory) {
        return checksums;
    }

    function indexOf(string memory _checksum) private view returns (uint256) {
        for (uint256 i = 0; i < checksums.length; i++) {
            if (keccak256(bytes(checksums[i])) == keccak256(bytes(_checksum))) {
                return i;
            }
        }
        revert("Checksum not found");
    }

    function isCertificateValid(
        string memory _checksum,
        Recipient memory _recipient,
        uint256 _days_valid,
        string memory _cert_name,
        string memory _issuer_identification_name
    ) private view returns (bool, string memory) {
        if ( bytes(_checksum).length == 0 ) {
            return (false, "File checksum must not be empty!");
        }

        if ( keccak256(bytes(certificates[_checksum].checksum)) == keccak256(bytes(_checksum)) ) {
            return (false, "Certificate already present!");
        }

        if ( bytes(_recipient.name).length == 0 ) {
            return (false, "Recipient name must not be empty!");
        }

        if ( bytes(_recipient.surname).length == 0 ) {
            return (false, "Recipient surname must not be empty!");
        }

        if ( bytes(_recipient.email).length == 0 ) {
            return (false, "Recipient email must not be empty!");
        }

        if ( bytes(_issuer_identification_name).length == 0 ) {
            return (false, "Issuer identification name must not be empty!");
        }

        if ( _days_valid <= 0 ) {
            return (false, "Contract must be valid for at least 1 day!");
        }

        if ( bytes(_cert_name).length == 0 ) {
            return (false, "Cert name must not be empty!");
        }

        return (true, "");
    }

    function bulkUploadCertificates(BulkCertificateData[] memory _bulkData)
        public
        onlyTrustedIssuer
    {
        for (uint256 i = 0; i < _bulkData.length; i++) {
            BulkCertificateData memory _certificate = _bulkData[i];

            bool valid;
            string memory reason;
            (valid, reason) = isCertificateValid(
                _certificate.checksum,
                _certificate.recipient,
                _certificate.days_valid,
                _certificate.cert_name,
                _certificate.issuer_identification_name
            );
            if (valid) {
                addVerifiedCertificate(
                    _certificate.checksum,
                    _certificate.recipient,
                    _certificate.days_valid,
                    _certificate.cert_name,
                    _certificate.issuer_identification_name
                );
            } else {
                emit FailedAddingCertificate(
                    _certificate.checksum,
                    reason
                );
            }
        }
    }
}
