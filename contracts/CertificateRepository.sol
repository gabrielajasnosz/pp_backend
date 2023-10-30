// SPDX-License-Identifier: MIT
pragma solidity >=0.4.22 <0.9.0;
 
contract CertificateRegistry {
    
    address contractOwner;

    struct Recipient { // recipient description
        // Should recipient have some kind of ID? Or should recipient have address?
        string name;
        string surname;
    }
 
    struct Certificate {
        string checksum; // unique hash of certificate generated on frontend
        uint256 issueDate;
        uint256 expireDate;
        address issuer;
        Recipient recipient;
    }
 
    mapping(string => Certificate) certificates;
 
    mapping(address => bool) public trustedIssuers;

    constructor() {
        contractOwner = msg.sender;
        trustedIssuers[msg.sender] = true; // contract owner address as first trusted issuer
    }
 
    modifier onlyTrustedIssuer() {
        require(trustedIssuers[msg.sender], "You are not a trusted issuer!");
        _;
    }
 
    function addCertificate(string memory _checksum, string memory _recipient_name, string memory _recipient_surname) public onlyTrustedIssuer {
        require(keccak256(bytes(certificates[_checksum].checksum)) != keccak256(bytes(_checksum)), "Certificate already present!");
        require(bytes(_checksum).length > 0, "File checksum must not be empty!");
        require(bytes(_recipient_name).length > 0, "Recipent name must not be empty!");
        require(bytes(_recipient_surname).length > 0, "Recipent surname must not be empty!");

        certificates[_checksum] = Certificate(_checksum, block.timestamp, block.timestamp + 30 days, msg.sender, Recipient(_recipient_name, _recipient_surname));
    }

    function addTrustedIssuer(address _issuer) public onlyTrustedIssuer {
        trustedIssuers[_issuer] = true;
    }

    function removeTrustedIssuer(address _issuer) public onlyTrustedIssuer {
        if(_issuer != contractOwner){
            trustedIssuers[_issuer] = false;
        }
    }

    // Anyone should be able to download certificate, unless recipients also has to be trusted
    function getCertificate(string memory _checksum) public view returns (Certificate memory result) {
        result = certificates[_checksum];
    }
}